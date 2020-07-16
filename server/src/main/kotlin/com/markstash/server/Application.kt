package com.markstash.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.markstash.api.errors.ErrorResponse
import com.markstash.api.errors.NotFoundException
import com.markstash.api.errors.ServerException
import com.markstash.server.auth.CurrentUser
import com.markstash.server.controllers.archives
import com.markstash.server.controllers.bookmarks
import com.markstash.server.controllers.sessions
import com.markstash.server.controllers.users
import com.markstash.server.db.Archive
import com.markstash.server.db.Database
import com.markstash.server.workers.JobProcessor
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import de.mkammerer.argon2.Argon2Factory
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.jwt
import io.ktor.features.CORS
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.resolveResource
import io.ktor.locations.Locations
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.serialization.json
import io.ktor.server.cio.CIO
import io.ktor.server.engine.ApplicationEngineEnvironment
import io.ktor.server.engine.addShutdownHook
import io.ktor.server.engine.commandLineEnvironment
import io.ktor.server.engine.embeddedServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.MissingFieldException
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
                    archiveAdapter = Archive.Adapter(
                        typeAdapter = EnumColumnAdapter(),
                        statusAdapter = EnumColumnAdapter()
                    ))
            }
            single { Settings(get()) }
            single { Argon2Factory.create() }
            single(named(Constants.Jwt.ISSUER)) { jwtIssuer }
            single(named(Constants.Jwt.AUDIENCE)) { jwtAudience }
            single(named(Constants.Jwt.REALM)) { jwtRealm }
            single(named(Constants.Jwt.SECRET)) { jwtSecret }
            single(named(Constants.Jwt.ALGORITHM)) { jwtAlgorithm }
            single { JobProcessor(this@main) }
            single(named(Constants.Storage.ARCHIVE_DIR)) { environment.config.propertyOrNull("markstash.archive_dir")?.getString() ?: "archives" }
        })
    }

    install(StatusPages) {
        exception<Throwable> { cause ->
            call.respond(HttpStatusCode.InternalServerError)
            throw cause
        }

        exception<MissingFieldException> { cause ->
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
        jwt {
            realm = jwtRealm
            verifier(
                JWT.require(jwtAlgorithm)
                    .withIssuer(jwtIssuer)
                    .withAudience(jwtAudience)
                    .build()
            )
            validate { credential ->
                if (!credential.payload.audience.contains(jwtAudience)) return@validate null
                val apiKey = credential.payload.getClaim("apiKey").asString() ?: return@validate null
                val db = get<Database>()
                CurrentUser(db.userQueries.findByApiKey(apiKey).executeAsOne())
            }
        }
    }

    install(CORS) {
        // Doesn't currently work for chrome-extension, see https://github.com/ktorio/ktor/issues/1656
        host("*", schemes = listOf("chrome-extension", "http", "https"))
        allowCredentials = true
        allowNonSimpleContentTypes = true
    }

    install(Locations)
    install(Routing) {
        route("/api") {
            sessions()
            users()
            archives()

            authenticate {
                bookmarks()
            }
        }

        get("{slug...}") {
            val relativePath = (call.parameters.getAll("slug")?.joinToString(File.separator) ?: "")
                .ifBlank { "index.html" }
                .replace("..", "")
            val content = call.resolveResource(relativePath, "assets")
                ?: call.resolveResource("index.html", "assets")
                ?: throw NotFoundException()
            call.respond(content)
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
