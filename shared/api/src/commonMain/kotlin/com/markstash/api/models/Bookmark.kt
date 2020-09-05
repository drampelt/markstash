@file:UseSerializers(InstantSerializer::class)

package com.markstash.api.models

import com.markstash.api.serializers.InstantSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class Bookmark(
    val id: Long,
    val title: String,
    val url: String,
    val iconArchiveId: Long?,
    val excerpt: String?,
    val author: String?,
    val tags: Set<String>,
    val createdAt: Instant,
    val updateAt: Instant,
    val archives: List<Archive>? = null
) {
    fun toResource() = Resource(
        type = Resource.Type.BOOKMARK,
        id = id,
        title = title,
        excerpt = excerpt,
        tags = tags,
        url = url,
        iconArchiveId = iconArchiveId,
        createdAt = createdAt,
        updatedAt = updateAt
    )
}
