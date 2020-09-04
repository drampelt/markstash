package com.markstash.server

import at.favre.lib.crypto.bcrypt.BCrypt
import at.favre.lib.crypto.bcrypt.LongPasswordStrategies
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.markstash.api.errors.ErrorResponse
import com.markstash.api.errors.NotFoundException
import com.markstash.api.errors.ServerException
import com.markstash.server.auth.CurrentUser
import com.markstash.server.auth.MarkstashSession
import com.markstash.server.controllers.archives
import com.markstash.server.controllers.bookmarks
import com.markstash.server.controllers.notes
import com.markstash.server.controllers.resources
import com.markstash.server.controllers.sessions
import com.markstash.server.controllers.tags
import com.markstash.server.controllers.users
import com.markstash.server.db.Database
import com.markstash.server.db.archiveAdapter
import com.markstash.server.db.bookmarkAdapter
import com.markstash.server.db.noteAdapter
import com.markstash.server.db.userAdapter
import com.markstash.server.workers.JobProcessor
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.jwt
import io.ktor.auth.session
import io.ktor.features.CORS
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.defaultForFilePath
import io.ktor.locations.Locations
import io.ktor.request.header
import io.ktor.response.respond
import io.ktor.response.respondOutputStream
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.serialization.json
import io.ktor.server.cio.CIO
import io.ktor.server.engine.ApplicationEngineEnvironment
import io.ktor.server.engine.addShutdownHook
import io.ktor.server.engine.commandLineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.sessions.SessionTransportTransformerMessageAuthentication
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.get
import sun.misc.Signal
import java.io.File
import kotlin.system.exitProcess

val mainStartTime: Long = System.currentTimeMillis()

fun main(args: Array<String>) {
    val applicationEnvironment = commandLineEnvironment(args)
    val server = embeddedServer(CIO, applicationEnvironment)
        .start(wait = true)
    server.addShutdownHook { server.stop(3000, 5000) }
}

fun Application.main() {
    log.info("Started server in ${System.currentTimeMillis() - mainStartTime}ms")
    val startTime = System.currentTimeMillis()

    val jwtIssuer = environment.config.property("jwt.issuer").getString()
    val jwtAudience = environment.config.property("jwt.audience").getString()
    val jwtRealm = environment.config.property("jwt.realm").getString()
    val jwtSecret = environment.config.property("jwt.secret").getString()
    val jwtAlgorithm = Algorithm.HMAC256(jwtSecret)
    val jwtVerifier = JWT.require(jwtAlgorithm)
        .withIssuer(jwtIssuer)
        .withAudience(jwtAudience)
        .build()

    val dbPath = environment.config.propertyOrNull("markstash.database_dir")?.getString() ?: "database"
    val dbFolder = File(dbPath)
    dbFolder.mkdirs()
    val dbFile = File(dbFolder, "markstash.db")

    val chromedriver = environment.config.propertyOrNull("markstash.chromedriver_bin")?.getString() ?: "/usr/local/bin/chromedriver"
    System.setProperty("webdriver.chrome.driver", chromedriver)

    install(CallLogging)

    install(Koin) {
        modules(module {
            single<SqlDriver> { JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}") }
            single {
                Database(
                    get(),
                    archiveAdapter = archiveAdapter,
                    bookmarkAdapter = bookmarkAdapter,
                    noteAdapter = noteAdapter,
                    userAdapter = userAdapter,
                )
            }
            single { Settings(get()) }
            single { BCrypt.with(LongPasswordStrategies.truncate(BCrypt.Version.VERSION_2A)) }
            single { BCrypt.verifyer(BCrypt.Version.VERSION_2A, LongPasswordStrategies.truncate(BCrypt.Version.VERSION_2A)) }
            single(named(Constants.Jwt.ISSUER)) { jwtIssuer }
            single(named(Constants.Jwt.AUDIENCE)) { jwtAudience }
            single(named(Constants.Jwt.REALM)) { jwtRealm }
            single(named(Constants.Jwt.SECRET)) { jwtSecret }
            single(named(Constants.Jwt.ALGORITHM)) { jwtAlgorithm }
            single { JobProcessor(this@main) }
            single(named(Constants.Storage.ARCHIVE_DIR)) { environment.config.propertyOrNull("markstash.archive_dir")?.getString() ?: "archives" }
            single(named(Constants.Binaries.CHROME_BIN)) { environment.config.propertyOrNull("markstash.chrome_bin")?.getString() ?: "chromium-browser" }
            single(named(Constants.Settings.CHROME_USE_DEV_SHM)) { environment.config.propertyOrNull("markstash.chrome_use_dev_shm")?.getString()?.toBoolean() ?: false }
        })
    }

    install(StatusPages) {
        exception<Throwable> { cause ->
            call.respond(HttpStatusCode.InternalServerError)
            throw cause
        }

        exception<SerializationException> { cause ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse.simple(cause.message ?: "Missing field"))
        }

        exception<ServerException> { cause ->
            call.respond(HttpStatusCode.fromValue(cause.status), cause.errorResponse)
        }
    }

    install(ContentNegotiation) {
        json()
    }

    install(Authentication) {
        jwt("jwt") {
            realm = jwtRealm
            verifier(jwtVerifier)
            validate { credential ->
                if (!credential.payload.audience.contains(jwtAudience)) return@validate null
                val apiKey = credential.payload.getClaim("apiKey").asString() ?: return@validate null
                val db = get<Database>()
                CurrentUser(db.userQueries.findByApiKey(apiKey).executeAsOne())
            }
            skipWhen { call ->
                call.sessions.get<MarkstashSession>() != null
            }
        }
        session<MarkstashSession>("cookie") {
            validate { session ->
                val credential = try {
                    jwtVerifier.verify(session.authToken)
                } catch (e: Throwable) {
                    return@validate null
                }

                if (!credential.audience.contains(jwtAudience)) return@validate null
                val apiKey = credential.getClaim("apiKey").asString() ?: return@validate null
                val db = get<Database>()
                CurrentUser(db.userQueries.findByApiKey(apiKey).executeAsOne())
            }
            skipWhen { call ->
                call.request.header("Authorization") != null && call.sessions.get<MarkstashSession>() == null
            }
        }
    }

    install(Sessions) {
        cookie<MarkstashSession>("markstash_session") {
            serializer = MarkstashSession.serializer
            cookie.path = "/"
            cookie.httpOnly = false
            transform(SessionTransportTransformerMessageAuthentication(jwtSecret.toByteArray()))
        }
    }

    install(CORS) {
        // Doesn't currently work for chrome-extension, see https://github.com/ktorio/ktor/issues/1656
        host("*", schemes = listOf("chrome-extension", "http", "https"))
        allowCredentials = true
        allowNonSimpleContentTypes = true
        methods.add(HttpMethod.Patch)
        methods.add(HttpMethod.Delete)
    }

    install(Locations)
    install(Routing) {
        route("/api") {
            sessions()
            users()

            authenticate("jwt", "cookie") {
                archives()
                bookmarks()
                notes()
                resources()
                tags()
            }
        }

        get("{slug...}") {
            var relativePath = (call.parameters.getAll("slug")?.joinToString(File.separator) ?: "")
                .ifBlank { "index.html" }
                .replace("..", "")

            val stream = javaClass.getResourceAsStream("/assets/$relativePath")
                ?: javaClass.getResourceAsStream("/assets/index.html")?.also { relativePath = "index.html" }
                ?: throw NotFoundException()

            stream.use { assetStream ->
                call.respondOutputStream(ContentType.defaultForFilePath(relativePath)) { assetStream.copyTo(this) }
            }
        }
    }

    GlobalScope.launch(Dispatchers.IO) {
        val dbStartTime = System.currentTimeMillis()

        if (!dbFile.exists()) {
            log.info("Creating database...")
            Database.Schema.create(get())
        }

        val settings = get<Settings>()
        if (settings.databaseVersion < Database.Schema.version) {
            log.info("Migrating database from version ${settings.databaseVersion} to ${Database.Schema.version}...")
            Database.Schema.migrate(get(), settings.databaseVersion, Database.Schema.version)
            settings.databaseVersion = Database.Schema.version
        }

        log.info("Finished db setup in ${System.currentTimeMillis() - dbStartTime}ms")

        get<JobProcessor>().start()
    }

    Signal.handle(Signal("INT")) { exitProcess(0) }
    Runtime.getRuntime().addShutdownHook(Thread {
        log.info("Shutting down...")
        val sqlDriver = get<SqlDriver>()
        sqlDriver.close()
        get<JobProcessor>().stop()
        log.info("Goodbye")
        (environment as? ApplicationEngineEnvironment)?.stop()
    })

    log.info("Finished configuration in ${System.currentTimeMillis() - startTime}ms")
}
