package com.markstash.api.bookmarks

import com.markstash.api.models.Archive
import com.markstash.api.models.Bookmark
import kotlinx.serialization.Serializable

@Serializable
data class ShowResponse(
    val bookmark: Bookmark,
    val archives: List<Archive>
)
