package com.markstash.web

import com.markstash.api.models.User
import com.markstash.api.sessions.LoginResponse
import com.markstash.shared.js.api.apiClient
import com.markstash.shared.js.api.usersApi
import jscookie.Cookies
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object Session {
    var isInitialLoad = true

    var currentUser: User? = null
        private set

    val isAuthenticated: Boolean
        get() = currentUser != null

    init {
        apiClient.authToken = Cookies.get("markstash_auth_token")
    }

    fun loginWithExistingToken(callback: (Boolean) -> Unit) {
        GlobalScope.launch {
            try {
                currentUser = usersApi.me()
                isInitialLoad = false
                callback(true)
            } catch (e: Throwable) {
                callback(false)
            }
        }
    }

    fun login(loginResponse: LoginResponse) {
        currentUser = loginResponse.user
        apiClient.authToken = loginResponse.authToken
        Cookies.set("markstash_auth_token", loginResponse.authToken)
    }

    fun logOut() {
        currentUser = null
        apiClient.authToken = null
    }
}
