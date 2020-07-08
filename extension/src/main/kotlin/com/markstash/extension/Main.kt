package com.markstash.extension

import browser.browser
import com.markstash.client.api.ApiClient
import com.markstash.client.api.BookmarksApi
import com.markstash.client.api.SessionsApi
import com.markstash.extension.background.backgroundMain
import com.markstash.extension.popup.popupMain
import kotlin.browser.window

fun main() {
    val getBackgroundPage = browser.extension.getBackgroundPage
    when {
        getBackgroundPage == null -> Unit // TODO: content scripts?
        getBackgroundPage() == window -> backgroundMain()
        else -> popupMain()
    }
}

val apiClient = ApiClient("http://localhost:8080/api")
val sessionsApi = SessionsApi(apiClient)
val bookmarksApi = BookmarksApi(apiClient)
