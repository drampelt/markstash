package com.markstash.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.markstash.api.ErrorResponse
import com.markstash.server.auth.CurrentUser
import com.markstash.server.controllers.bookmarks
import com.markstash.server.controllers.sessions
import com.markstash.server.controllers.users
import com.markstash.server.db.Database
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
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.NotFoundException
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Locations
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.serialization.json
import io.ktor.serialization.serialization
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.MissingFieldException
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.get
import java.io.File

fun Application.main() {
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

    install(CallLogging)

    install(Koin) {
        modules(module {
            single<SqlDriver> { JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}") }
            single { Database(get()) }
            single { Settings(get()) }
            single { Argon2Factory.create() }
            single(named(Constants.Jwt.ISSUER)) { jwtIssuer }
            single(named(Constants.Jwt.AUDIENCE)) { jwtAudience }
            single(named(Constants.Jwt.REALM)) { jwtRealm }
            single(named(Constants.Jwt.SECRET)) { jwtSecret }
            single(named(Constants.Jwt.ALGORITHM)) { jwtAlgorithm }
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

        exception<NotFoundException> { cause ->
            call.respond(HttpStatusCode.NotFound, ErrorResponse.simple(cause.message ?: "Not found"))
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

    install(Locations)
    install(Routing) {
        sessions()
        users()

        authenticate {
            bookmarks()
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
    }

    Runtime.getRuntime().addShutdownHook(Thread {
        log.info("Shutting down...")
        val sqlDriver = get<SqlDriver>()
        sqlDriver.close()
        log.info("Goodbye")
    })

    log.info("Finished configuration in ${System.currentTimeMillis() - startTime}ms")
}
