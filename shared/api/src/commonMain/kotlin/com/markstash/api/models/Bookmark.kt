package com.markstash.api.models

import kotlinx.serialization.Serializable

@Serializable
data class Bookmark(
    val id: Long,
    val title: String,
    val url: String,
    val tags: Set<String>
)
