package com.markstash.client.util

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

fun LocalDateTime.formatRelativeDisplay(): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val diff = now.date - this.date
    return when {
        diff.days < 1 -> {
            val hourDisplay = (hour % 12).takeUnless { it == 0 } ?: 12
            "$hourDisplay:${minute.toString().padStart(2, '0')} ${if (hour < 12) "am" else "pm"}"
        }
        diff.days == 1 -> "Yesterday"
        diff.days < 7 -> "${diff.days} days ago"
        else -> StringBuilder().apply {
            append(month.name.toLowerCase().capitalize())
            append(" ")
            append(dayOfMonth)
            if (diff.years > 0) {
                append(", ")
                append(year)
            }
        }.toString()
    }
}
