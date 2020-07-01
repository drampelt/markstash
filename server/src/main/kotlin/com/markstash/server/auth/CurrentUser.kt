package com.markstash.server.auth

import com.markstash.server.db.User
import io.ktor.auth.Principal

data class CurrentUser(
    val user: User
) : Principal
