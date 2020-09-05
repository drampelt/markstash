@file:UseSerializers(InstantSerializer::class)

package com.markstash.api.models

import com.markstash.api.serializers.InstantSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class Resource(
    val type: Type,
    val id: Long,
    val title: String?,
    val excerpt: String?,
    val tags: Set<String>,
    val url: String?,
    val iconArchiveId: Long?,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    enum class Type {
        BOOKMARK,
        NOTE,
    }
}
