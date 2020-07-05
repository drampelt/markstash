package com.markstash.extension.popup

import browser.browser
import com.markstash.extension.dyn
import kotlin.browser.document

fun popupMain() {
    browser.tabs.query(dyn {
        active = true
        currentWindow = true
    }).then { (tab) ->
        document.getElementById("page-title")?.innerHTML = "Title: ${tab.title}"
        document.getElementById("page-url")?.innerHTML = "Title: ${tab.url}"
    }
}
