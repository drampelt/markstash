package com.markstash.web

import react.child
import react.dom.*
import kotlin.browser.document

fun main() {
    render(document.getElementById("app")) {
        child(routes)
    }
}
