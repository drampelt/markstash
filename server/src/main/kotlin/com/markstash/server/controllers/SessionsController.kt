package com.markstash.server.controllers

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.markstash.api.errors.ForbiddenException
import com.markstash.api.models.User
import com.markstash.api.sessions.LoginRequest
import com.markstash.api.sessions.LoginResponse
import com.markstash.server.Constants
import com.markstash.server.db.Database
import de.mkammerer.argon2.Argon2
import io.ktor.application.call
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
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
        val request = call.receive<LoginRequest>()
        val user = db.userQueries.findByEmail(request.email).executeAsOneOrNull() ?: throw ForbiddenException("Invalid email or password")
        if (!argon2.verify(user.password, request.password.toCharArray())) throw ForbiddenException("Invalid email or password")

        val jwtToken = JWT.create()
            .withSubject("Authentication")
            .withIssuer(jwtIssuer)
            .withAudience(jwtAudience)
            .withClaim("apiKey", user.apiKey)
            .sign(jwtAlgorithm)

        call.respond(LoginResponse(
            user = User(id = user.id, email = user.email),
            authToken = jwtToken
        ))
    }
}
