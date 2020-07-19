package com.markstash.api.models

import kotlinx.serialization.Serializable

@Serializable
data class Archive(
    val id: Long,
    val key: String,
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
        SCREENSHOT,
        SCREENSHOT_FULL,
        HAR,
        WARC,
    }

    @Serializable
    enum class Status {
        PROCESSING,
        COMPLETED,
        FAILED,
    }
}
