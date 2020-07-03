package com.markstash.api.sessions

import com.markstash.api.models.User
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val user: User,
    val authToken: String
)
