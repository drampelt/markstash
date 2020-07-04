package com.markstash.extension

import browser.browser
import com.markstash.api.sessions.LoginRequest
import com.markstash.client.api.ApiClient
import com.markstash.client.api.SessionsApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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

    val apiClient = ApiClient("http://localhost:8080")
    val sessionsApi = SessionsApi(apiClient)
    GlobalScope.launch {
        val loginResponse = sessionsApi.login(LoginRequest("email", "password"))
        document.getElementById("info")?.innerHTML = JSON.stringify(loginResponse)
    }
}

fun <T> dyn(block: T.() -> Unit): T = js("{}").unsafeCast<T>().apply(block)
