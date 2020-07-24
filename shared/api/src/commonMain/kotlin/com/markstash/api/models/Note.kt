package com.markstash.api.models

import kotlinx.serialization.Serializable

@Serializable
data class Note(
    val id: Long,
    val title: String?,
    val excerpt: String?,
    val content: String?,
    val tags: Set<String>,
    val createdAt: String,
    val updatedAt: String
) {
    companion object {
        private val headingRegex = Regex("^#+ ?")
        private val nonAlphaRegex = Regex("[^A-Za-z0-9 .,;-]")

        fun parseMetadata(content: String?): Pair<String?, String?> {
            if (content == null) return Pair(null, null)

            var title: String? = null
            var excerpt: String? = null
            for (line in content.lineSequence()) {
                if (line.isBlank()) continue
                if (title == null) {
                    title = line.replace(headingRegex, "")
                } else {
                    excerpt = line.replace(nonAlphaRegex, "")
                    break
                }
            }

            return Pair(title, excerpt)
        }
    }

    fun toResource() = Resource(
        type = Resource.Type.NOTE,
        id = id,
        title = title,
        excerpt = excerpt,
        tags = tags,
        url = null,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
