package com.markstash.api.archives

import com.markstash.api.models.Archive
import kotlinx.serialization.Serializable

@Serializable
data class FetchRequest(
    val archiveTypes: List<Archive.Type>
)
