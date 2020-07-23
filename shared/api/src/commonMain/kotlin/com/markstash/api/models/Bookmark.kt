package com.markstash.api.models

import kotlinx.serialization.Serializable

@Serializable
data class Bookmark(
    val id: Long,
    val title: String,
    val url: String,
    val excerpt: String?,
    val author: String?,
    val tags: Set<String>,
    val createdAt: String,
    val updateAt: String
) {
    fun toResource() = Resource(
        type = Resource.Type.BOOKMARK,
        id = id,
        title = title,
        excerpt = excerpt,
        tags = tags,
        url = url,
        createdAt = createdAt,
        updatedAt = updateAt
    )
}
