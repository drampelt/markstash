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
)
