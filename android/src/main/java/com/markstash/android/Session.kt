package com.markstash.android

import androidx.compose.runtime.ambientOf
import com.markstash.api.models.User
import com.markstash.api.sessions.LoginResponse
import com.markstash.client.api.ApiClient

class Session {
    companion object {
        val ambient = ambientOf<Session>()
    }

    var user: User? = null
        private set

    var authToken: String? = null
        private set(value) {
            field = value
            apiClient.authToken = value
        }

    val isLoggedIn: Boolean
        get() = user != null

    val apiClient = ApiClient(baseUrl = "http://192.168.2.42:8080/api")

    fun login(loginResponse: LoginResponse) {
        user = loginResponse.user
        authToken = loginResponse.authToken
    }

    fun logout() {
        user = null
        authToken = null
    }

    fun requireUser() = user ?: throw IllegalStateException("Not logged in")
}
