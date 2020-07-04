package com.markstash.server.auth

import com.markstash.server.db.User
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.Principal
import io.ktor.auth.authentication
import io.ktor.util.pipeline.PipelineContext

data class CurrentUser(
    val user: User
) : Principal

val PipelineContext<Unit, ApplicationCall>.currentUser
    get() = call.authentication.principal<CurrentUser>()!!
