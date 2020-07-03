package com.markstash.api.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long,
    val email: String
)
