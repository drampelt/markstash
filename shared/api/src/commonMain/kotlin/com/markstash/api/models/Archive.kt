package com.markstash.api.models

import kotlinx.serialization.Serializable

@Serializable
data class Archive(
    val id: Long,
    val bookmarkId: Long,
    val type: Type,
    val status: Status,
    val path: String?,
    val data: String?
) {
    @Serializable
    enum class Type {
        ORIGINAL,
        PLAIN,
        READABILITY,
        MONOLITH,
        MONOLITH_READABILITY,
        SCREENSHOT_FULL,
    }

    @Serializable
    enum class Status {
        PROCESSING,
        COMPLETED,
        FAILED,
    }
}
