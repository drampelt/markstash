@file:UseSerializers(InstantSerializer::class)

package com.markstash.api.models

import com.markstash.api.serializers.InstantSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class Archive(
    val id: Long,
    val key: String,
    val bookmarkId: Long,
    val type: Type,
    val status: Status,
    val path: String?,
    val data: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    @Serializable
    enum class Type(val previewType: PreviewType, val displayName: String) {
        ORIGINAL(PreviewType.INLINE, "Original"),
        PLAIN(PreviewType.NONE, "Plain Text"),
        PDF(PreviewType.INLINE, "PDF"),
        MONOLITH(PreviewType.INLINE, "Monolith"),
        READABILITY(PreviewType.INLINE, "Readability"),
        MONOLITH_READABILITY(PreviewType.INLINE, "Readability (Monolith)"),
        SCREENSHOT(PreviewType.INLINE, "Screenshot"),
        SCREENSHOT_FULL(PreviewType.INLINE, "Full Page Screenshot"),
        FEATURE_IMAGE(PreviewType.INLINE, "Feature Image"),
        HAR(PreviewType.DOWNLOAD, "HAR"),
        WARC(PreviewType.DOWNLOAD, "WARC"),
        FAVICON(PreviewType.DOWNLOAD, "Favicon"),
        ;

        enum class PreviewType {
            NONE,
            INLINE,
            DOWNLOAD,
            EXTERNAL,
            ;
        }
    }

    @Serializable
    enum class Status {
        PROCESSING,
        COMPLETED,
        FAILED,
    }
}
