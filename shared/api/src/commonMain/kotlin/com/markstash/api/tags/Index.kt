package com.markstash.api.tags

import kotlinx.serialization.Serializable

@Serializable
data class IndexResponse(
    val tags: List<Tag>
) {
    @Serializable
    data class Tag(
        val name: String,
        val count: Long
    )
}
