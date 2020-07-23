package com.markstash.api.models

import kotlinx.serialization.Serializable

@Serializable
data class Resource(
    val type: Type,
    val id: Long,
    val title: String?,
    val excerpt: String?,
    val tags: Set<String>,
    val url: String?,
    val createdAt: String,
    val updatedAt: String
) {
    enum class Type {
        BOOKMARK,
        NOTE,
    }
}
