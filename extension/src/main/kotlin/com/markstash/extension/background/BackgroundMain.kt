package com.markstash.extension.background

import com.markstash.api.sessions.LoginRequest
import com.markstash.client.api.ApiClient
import com.markstash.client.api.SessionsApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun backgroundMain() {
    val apiClient = ApiClient("http://localhost:8080")
    val sessionsApi = SessionsApi(apiClient)
    GlobalScope.launch {
        val loginResponse = sessionsApi.login(LoginRequest("email", "password"))
        console.log("login response", loginResponse)
    }
}
