package com.markstash.server.controllers

import com.markstash.server.auth.ApiKeyGenerator
import com.markstash.server.auth.CurrentUser
import com.markstash.server.db.Database
import com.markstash.server.main
import de.mkammerer.argon2.Argon2
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.response.respondText
import io.ktor.routing.Route
import org.koin.ktor.ext.inject

@Location("/users")
class Users {
    @Location("")
    class Register

    @Location("/me")
    class Me
}

fun Route.users() {
    val db: Database by inject()
    val argon2: Argon2 by inject()

    post<Users.Register> {
        val email = call.parameters["email"] ?: return@post call.respondText("Invalid email", status = HttpStatusCode.BadRequest)
        val password = call.parameters["password"] ?: return@post call.respondText("Invalid password", status = HttpStatusCode.BadRequest)
        val hashed = argon2.hash(8, 65536, 8, password.toCharArray())
        db.userQueries.insert(email, hashed, ApiKeyGenerator.generate())
        call.respondText("Success")
    }

    authenticate {
        get<Users.Me> {
            val currentUser = call.authentication.principal<CurrentUser>() ?: return@get
            call.respondText(currentUser.user.email)
        }
    }
}
