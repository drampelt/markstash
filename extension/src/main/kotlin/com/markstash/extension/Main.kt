package com.markstash.extension

import browser.browser
import com.markstash.extension.background.backgroundMain
import com.markstash.extension.popup.popupMain
import com.markstash.shared.js.api.apiClient
import kotlin.browser.window

fun main() {
    apiClient.baseUrl = "http://localhost:8080/api"

    val getBackgroundPage = browser.extension.getBackgroundPage
    when {
        getBackgroundPage == null -> Unit // TODO: content scripts?
        getBackgroundPage() == window -> backgroundMain()
        else -> popupMain()
    }
}
