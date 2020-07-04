package com.markstash.api.bookmarks

import com.markstash.api.models.Bookmark
import kotlinx.serialization.Serializable

@Serializable
data class CreateRequest(
    val title: String,
    val url: String
)

typealias CreateResponse = Bookmark
