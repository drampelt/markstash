package com.markstash.api.bookmarks

import com.markstash.api.models.Bookmark
import kotlinx.serialization.Serializable

@Serializable
data class UpdateRequest(
    val tags: Set<String>
)

typealias UpdateResponse = Bookmark
