package com.markstash.extension

import browser.browser
import kotlin.browser.document

fun main() {
    console.log("Browser extension!")

    browser.tabs.query(dyn {
        active = true
        currentWindow = true
    }).then { (tab) ->
        document.getElementById("page-title")?.innerHTML = "Title: ${tab.title}"
        document.getElementById("page-url")?.innerHTML = "Title: ${tab.url}"
    }
}

fun <T> dyn(block: T.() -> Unit): T = js("{}").unsafeCast<T>().apply(block)
