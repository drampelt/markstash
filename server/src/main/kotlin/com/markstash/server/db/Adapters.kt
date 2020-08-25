package com.markstash.server.db

import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.EnumColumnAdapter
import kotlinx.datetime.Instant

private val instantAdapter = object : ColumnAdapter<Instant, String> {
    override fun decode(databaseValue: String): Instant = Instant.parse(databaseValue)

    override fun encode(value: Instant): String = value.toString()
}

val archiveAdapter = Archive.Adapter(
    typeAdapter = EnumColumnAdapter(),
    statusAdapter = EnumColumnAdapter(),
    createdAtAdapter = instantAdapter,
    updatedAtAdapter = instantAdapter,
)

val bookmarkAdapter = Bookmark.Adapter(
    createdAtAdapter = instantAdapter,
    updatedAtAdapter = instantAdapter,
)

val noteAdapter = Note.Adapter(
    createdAtAdapter = instantAdapter,
    updatedAtAdapter = instantAdapter,
)

val userAdapter = User.Adapter(
    createdAtAdapter = instantAdapter,
    updatedAtAdapter = instantAdapter,
)
