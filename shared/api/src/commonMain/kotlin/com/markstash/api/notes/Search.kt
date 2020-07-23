package com.markstash.api.notes

import com.markstash.api.models.Note
import kotlinx.serialization.Serializable

@Serializable
data class SearchRequest(
    val query: String
)

@Serializable
data class SearchResponse(
    val results: List<Note>
)
