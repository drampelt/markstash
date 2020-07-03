package com.markstash.api

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val errors: List<ErrorMessage>
) {
    companion object {
        fun simple(message: String) = ErrorResponse(listOf(ErrorMessage(message)))
    }

    @Serializable
    data class ErrorMessage(
        val message: String,
        val target: String? = null
    )
}
