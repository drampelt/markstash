package com.markstash.server.controllers

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.markstash.server.Constants
import com.markstash.server.db.Database
import de.mkammerer.argon2.Argon2
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.response.respondText
import io.ktor.routing.Route
import org.koin.core.qualifier.named
import org.koin.ktor.ext.inject

@Location("/login")
class Login

fun Route.sessions() {
    val db: Database by inject()
    val argon2: Argon2 by inject()
    val jwtIssuer: String by inject(named(Constants.Jwt.ISSUER))
    val jwtAudience: String by inject(named(Constants.Jwt.AUDIENCE))
    val jwtAlgorithm: Algorithm by inject(named(Constants.Jwt.ALGORITHM))

    post<Login> {
        val email = call.parameters["email"] ?: return@post call.respondText("Invalid email", status = HttpStatusCode.BadRequest)
        val password = call.parameters["password"] ?: return@post call.respondText("Invalid password", status = HttpStatusCode.BadRequest)
        val user = db.userQueries.findByEmail(email).executeAsOneOrNull() ?: return@post call.respondText("Invalid email or password", status = HttpStatusCode.Forbidden)
        if (!argon2.verify(user.password, password.toCharArray())) return@post call.respondText("Invalid email or password", status = HttpStatusCode.Forbidden)

        val jwtToken = JWT.create()
            .withSubject("Authentication")
            .withIssuer(jwtIssuer)
            .withAudience(jwtAudience)
            .withClaim("apiKey", user.apiKey)
            .sign(jwtAlgorithm)

        call.respondText(jwtToken)
    }
}
