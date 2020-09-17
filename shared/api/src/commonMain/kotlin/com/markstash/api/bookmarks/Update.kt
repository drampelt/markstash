package com.markstash.api.bookmarks

import com.markstash.api.models.Bookmark
import kotlinx.serialization.Serializable

@Serializable
data class UpdateRequest(
    val title: String? = null,
    val excerpt: String? = null,
    val tags: Set<String>? = null,
)

typealias UpdateResponse = Bookmark
