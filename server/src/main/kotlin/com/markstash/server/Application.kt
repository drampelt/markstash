package com.markstash.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.markstash.server.auth.ApiKeyGenerator
import com.markstash.server.auth.CurrentUser
import com.markstash.server.db.Database
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import de.mkammerer.argon2.Argon2
import de.mkammerer.argon2.Argon2Factory
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.auth.jwt.jwt
import io.ktor.features.CallLogging
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.dsl.module
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.get

fun Application.main() {
    val jwtIssuer = environment.config.property("jwt.issuer").getString()
    val jwtAudience = environment.config.property("jwt.audience").getString()
    val jwtRealm = environment.config.property("jwt.realm").getString()
    val jwtSecret = environment.config.property("jwt.secret").getString()
    val jwtAlgorithm = Algorithm.HMAC256(jwtSecret)

    install(CallLogging)

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

    install(Routing) {
        get("/") {
            call.respondText("Hello, World!", ContentType.Text.Html)
        }

        post("/users") {
            val db = this@main.get<Database>()
            val email = call.parameters["email"] ?: return@post call.respondText("Invalid email", status = HttpStatusCode.BadRequest)
            val password = call.parameters["password"] ?: return@post call.respondText("Invalid password", status = HttpStatusCode.BadRequest)
            val hashed = this@main.get<Argon2>().hash(8, 65536, 8, password.toCharArray())
            db.userQueries.insert(email, hashed, ApiKeyGenerator.generate())
            call.respondText("Success")
        }

        post("/login") {
            val db = this@main.get<Database>()
            val email = call.parameters["email"] ?: return@post call.respondText("Invalid email", status = HttpStatusCode.BadRequest)
            val password = call.parameters["password"] ?: return@post call.respondText("Invalid password", status = HttpStatusCode.BadRequest)
            val user = db.userQueries.findByEmail(email).executeAsOneOrNull() ?: return@post call.respondText("Invalid email or password", status = HttpStatusCode.Forbidden)
            if (!this@main.get<Argon2>().verify(user.password, password.toCharArray())) return@post call.respondText("Invalid email or password", status = HttpStatusCode.Forbidden)
            val jwtToken = JWT.create()
                .withSubject("Authentication")
                .withIssuer(jwtIssuer)
                .withAudience(jwtAudience)
                .withClaim("apiKey", user.apiKey)
                .sign(jwtAlgorithm)
            call.respondText(jwtToken)
        }

        authenticate {
            get("/secret") {
                val currentUser = call.authentication.principal<CurrentUser>()!!
                call.respondText("Logged in as: ${currentUser.user.email}")
            }
        }
    }

    install(Koin) {
        modules(module {
            single<SqlDriver> { JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY) }
            single { Database(get()) }
            single { Argon2Factory.create() }
        })
    }

    GlobalScope.launch(Dispatchers.IO) {
        Database.Schema.create(get())
    }

    Runtime.getRuntime().addShutdownHook(Thread {
        log.info("Shutting down...")
        val sqlDriver = get<SqlDriver>()
        sqlDriver.close()
        log.info("Goodbye")
    })
}
