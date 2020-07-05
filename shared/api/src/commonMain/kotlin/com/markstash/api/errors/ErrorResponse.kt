package com.markstash.api.errors

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val errors: List<ErrorMessage>
) {
    companion object {
        fun simple(message: String) = ErrorResponse(
            listOf(
                ErrorMessage(message)
            )
        )
    }

    fun message(): String {
        if (errors.isEmpty()) return "An unknown error occurred"
        return errors.joinToString(", ") { (message, target) ->
            if (target == null) message else "$target $message"
        }
    }

    @Serializable
    data class ErrorMessage(
        val message: String,
        val target: String? = null
    )
}
