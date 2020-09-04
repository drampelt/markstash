package com.markstash.server.controllers

import at.favre.lib.crypto.bcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.markstash.api.errors.ForbiddenException
import com.markstash.api.models.User
import com.markstash.api.sessions.LoginRequest
import com.markstash.api.sessions.LoginResponse
import com.markstash.server.Constants
import com.markstash.server.auth.MarkstashSession
import com.markstash.server.db.Database
import io.ktor.application.call
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import org.koin.core.qualifier.named
import org.koin.ktor.ext.inject

@Location("/login")
class Login

fun Route.sessions() {
    val db: Database by inject()
    val bcrypt: BCrypt.Verifyer by inject()
    val jwtIssuer: String by inject(named(Constants.Jwt.ISSUER))
    val jwtAudience: String by inject(named(Constants.Jwt.AUDIENCE))
    val jwtAlgorithm: Algorithm by inject(named(Constants.Jwt.ALGORITHM))

    post<Login> {
        val request = call.receive<LoginRequest>()
        val user = db.userQueries.findByEmail(request.email).executeAsOneOrNull() ?: throw ForbiddenException("Invalid email or password")
        if (!bcrypt.verify(request.password.toCharArray(), user.password).verified) throw ForbiddenException("Invalid email or password")

        val jwtToken = JWT.create()
            .withSubject("Authentication")
            .withIssuer(jwtIssuer)
            .withAudience(jwtAudience)
            .withClaim("apiKey", user.apiKey)
            .sign(jwtAlgorithm)

        call.sessions.set(MarkstashSession(jwtToken))

        call.respond(LoginResponse(
            user = User(
                id = user.id,
                email = user.email,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt,
            ),
            authToken = jwtToken
        ))
    }
}
