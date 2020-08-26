package com.markstash.client.util

private val domainRegex by lazy { Regex("^(?:https?://)?(?:[^@/\\n]+@)?(?:www\\.)?([^:/?\\n]+)") }

fun String.parseDomainFromUrl(): String? {
    return domainRegex.find(this)?.groups?.get(1)?.value
}
