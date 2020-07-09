package com.markstash.web

import com.markstash.shared.js.api.apiClient
import react.child
import react.dom.*
import kotlin.browser.document
import kotlin.browser.window

fun main() {
    apiClient.baseUrl = "${window.location.href}api"

    render(document.getElementById("app")) {
        child(routes)
    }
}
