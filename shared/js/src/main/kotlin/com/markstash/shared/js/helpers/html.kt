package com.markstash.shared.js.helpers

import react.RBuilder
import react.dom.*

fun RBuilder.rawHtml(classes: String? = null, html: () -> String) {
    div(classes) {
        val htmlObj = js("{}")
        htmlObj.__html = html()
        attrs["dangerouslySetInnerHTML"] = htmlObj
    }
}
