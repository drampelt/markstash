@file:UseSerializers(InstantSerializer::class)

package com.markstash.api.models

import com.markstash.api.serializers.InstantSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class Archive(
    val id: Long,
    val key: String,
    val bookmarkId: Long,
    val type: Type,
    val status: Status,
    val path: String?,
    val data: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
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
        PDF,
        FAVICON,
    }

    @Serializable
    enum class Status {
        PROCESSING,
        COMPLETED,
        FAILED,
    }
}
