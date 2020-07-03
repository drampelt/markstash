package com.markstash.server.controllers

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.markstash.api.users.RegisterRequest
import com.markstash.api.users.RegisterResponse
import com.markstash.api.models.User
import com.markstash.server.Constants
import com.markstash.server.auth.ApiKeyGenerator
import com.markstash.server.auth.CurrentUser
import com.markstash.server.db.Database
import de.mkammerer.argon2.Argon2
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
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
    val argon2: Argon2 by inject()
    val jwtIssuer: String by inject(named(Constants.Jwt.ISSUER))
    val jwtAudience: String by inject(named(Constants.Jwt.AUDIENCE))
    val jwtAlgorithm: Algorithm by inject(named(Constants.Jwt.ALGORITHM))

    post<Users.Register> {
        val request = call.receive<RegisterRequest>()
        if (request.password.length < 8 || request.password != request.passwordConfirmation) return@post call.respondText("Passwords must be 8 characters and must match confirmation", status = HttpStatusCode.BadRequest)

        val hashed = argon2.hash(8, 65536, 8, request.password.toCharArray())
        db.userQueries.insert(request.email, hashed, ApiKeyGenerator.generate())
        val user = db.userQueries.findByEmail(request.email).executeAsOne()

        val jwtToken = JWT.create()
            .withSubject("Authentication")
            .withIssuer(jwtIssuer)
            .withAudience(jwtAudience)
            .withClaim("apiKey", user.apiKey)
            .sign(jwtAlgorithm)

        call.respond(RegisterResponse(
            user = User(id = user.id, email = user.email),
            authToken = jwtToken
        ))
    }

    authenticate {
        get<Users.Me> {
            val currentUser = call.authentication.principal<CurrentUser>() ?: return@get
            call.respond(
                User(
                    id = currentUser.user.id,
                    email = currentUser.user.email
                )
            )
        }
    }
}
