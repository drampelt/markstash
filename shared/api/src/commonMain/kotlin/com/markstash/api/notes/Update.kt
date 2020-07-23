package com.markstash.api.notes

import com.markstash.api.models.Note
import kotlinx.serialization.Serializable

@Serializable
data class UpdateRequest(
    val content: String?,
    val tags: Set<String>
)

typealias UpdateResponse = Note
