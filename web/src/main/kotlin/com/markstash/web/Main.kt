package com.markstash.web

import com.markstash.shared.js.api.apiClient
import react.child
import react.dom.*
import kotlin.browser.document
import kotlin.browser.window

fun main() {
    js("require('css/main.css');")
    apiClient.baseUrl = "${window.location.protocol}//${window.location.hostname}:${window.location.port}/api"

    render(document.getElementById("app")) {
        child(routes)
    }
}
