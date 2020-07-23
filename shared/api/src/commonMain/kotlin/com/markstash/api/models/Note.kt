package com.markstash.api.models

import kotlinx.serialization.Serializable

@Serializable
data class Note(
    val id: Long,
    val title: String?,
    val excerpt: String?,
    val content: String?,
    val tags: Set<String>,
    val createdAt: String,
    val updatedAt: String
) {
    fun toResource() = Resource(
        type = Resource.Type.NOTE,
        id = id,
        title = title,
        excerpt = excerpt,
        tags = tags,
        url = null,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
