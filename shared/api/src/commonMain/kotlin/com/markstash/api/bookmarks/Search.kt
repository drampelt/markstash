package com.markstash.api.bookmarks

import com.markstash.api.models.Bookmark
import kotlinx.serialization.Serializable

@Serializable
data class SearchRequest(
    val query: String
)

@Serializable
data class SearchResponse(
    val results: List<Bookmark>
)
