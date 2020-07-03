package com.markstash.api.users

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val passwordConfirmation: String
)
