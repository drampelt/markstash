@file:UseSerializers(InstantSerializer::class)

package com.markstash.api.models

import com.markstash.api.serializers.InstantSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class User(
    val id: Long,
    val email: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)
