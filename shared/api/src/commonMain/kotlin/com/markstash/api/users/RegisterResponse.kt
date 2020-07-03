package com.markstash.api.users

import com.markstash.api.models.User
import kotlinx.serialization.Serializable

@Serializable
data class RegisterResponse(
    val user: User,
    val authToken: String
)
