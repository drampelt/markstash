package com.markstash.web

import react.dom.*
import kotlin.browser.document

fun main() {
    render(document.getElementById("app")) {
        h1 { +"Hello, world!" }
    }
}
