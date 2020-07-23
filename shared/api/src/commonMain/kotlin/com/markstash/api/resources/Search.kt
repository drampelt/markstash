package com.markstash.api.resources

import com.markstash.api.models.Resource
import kotlinx.serialization.Serializable

@Serializable
data class SearchRequest(
    val query: String
)

@Serializable
data class SearchResponse(
    val results: List<Resource>
)
