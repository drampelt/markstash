package com.markstash.extension

import browser.tabs.QueryInfo
import kotlin.browser.document

fun main() {
    console.log("Browser extension!")
    browser.tabs.query(QueryInfo {
        active = true
        currentWindow = true
    }).then { (tab) ->
        document.getElementById("page-title")?.innerHTML = "Name: ${tab.title}"
        document.getElementById("page-url")?.innerHTML = "URL: ${tab.url}"
    }
}
