package com.markstash.server.controllers

import at.favre.lib.crypto.bcrypt.BCrypt
import at.favre.lib.crypto.bcrypt.LongPasswordStrategies
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.markstash.api.errors.ValidationException
import com.markstash.api.models.User
import com.markstash.api.users.RegisterRequest
import com.markstash.api.users.RegisterResponse
import com.markstash.server.Constants
import com.markstash.server.auth.ApiKeyGenerator
import com.markstash.server.auth.MarkstashSession
import com.markstash.server.auth.currentUser
import com.markstash.server.db.Database
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import org.koin.core.qualifier.named
import org.koin.ktor.ext.inject

@Location("/users")
class Users {
    @Location("")
    class Register(val parent: Users)

    @Location("/me")
    class Me(val parent: Users)
}

fun Route.users() {
    val db: Database by inject()
    val bcrypt: BCrypt.Hasher by inject()
    val jwtIssuer: String by inject(named(Constants.Jwt.ISSUER))
    val jwtAudience: String by inject(named(Constants.Jwt.AUDIENCE))
    val jwtAlgorithm: Algorithm by inject(named(Constants.Jwt.ALGORITHM))

    post<Users.Register> {
        val request = call.receive<RegisterRequest>()
        if (request.password.length < 8) throw ValidationException("password", "must be at least 8 characters")
        if (request.password != request.passwordConfirmation) throw ValidationException("password confirmation", "must match password")

        val hashed = bcrypt.hashToString(12, request.password.toCharArray())
        db.userQueries.insert(request.email, hashed, ApiKeyGenerator.generate())
        val user = db.userQueries.findByEmail(request.email).executeAsOne()

        val jwtToken = JWT.create()
            .withSubject("Authentication")
            .withIssuer(jwtIssuer)
            .withAudience(jwtAudience)
            .withClaim("apiKey", user.apiKey)
            .sign(jwtAlgorithm)

        call.sessions.set(MarkstashSession(jwtToken))

        call.respond(RegisterResponse(
            user = User(
                id = user.id,
                email = user.email,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt,
            ),
            authToken = jwtToken
        ))
    }

    authenticate("jwt", "cookie") {
        get<Users.Me> {
            call.respond(
                User(
                    id = currentUser.user.id,
                    email = currentUser.user.email,
                    createdAt = currentUser.user.createdAt,
                    updatedAt = currentUser.user.updatedAt,
                )
            )
        }
    }
}
