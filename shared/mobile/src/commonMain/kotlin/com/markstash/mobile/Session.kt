package com.markstash.mobile

import com.markstash.api.models.User
import com.markstash.api.sessions.LoginResponse
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Session(
    private val listener: Listener? = null,
) {
    companion object {
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_USER = "user"

        const val DEFAULT_BASE_URL = "https://app.markstash.com/api"
    }

    // TODO: should probably use separate secure settings for auth token
    private val settings: Settings = Settings()

    var user: User? = null
        private set(value) {
            field = value
            _isLoggedInFlow.value = value != null
        }

    var authToken: String? = null
        private set(value) {
            field = value
            listener?.onAuthTokenChange(value)
        }

    var baseUrl: String = settings.getString(KEY_BASE_URL, DEFAULT_BASE_URL)
        set(value) {
            val newUrl = if (value.isBlank()) DEFAULT_BASE_URL else value
            field = newUrl
            settings.putString(KEY_BASE_URL, newUrl)
            listener?.onBaseUrlChange(newUrl)
        }

    val isLoggedIn: Boolean
        get() = user != null

    private val _isLoggedInFlow = MutableStateFlow(isLoggedIn)
    val isLoggedInFlow: StateFlow<Boolean> = _isLoggedInFlow

    init {
        settings.getStringOrNull(KEY_USER)?.let { userString ->
            val loginResponse = Json.decodeFromString<LoginResponse>(userString)
            user = loginResponse.user
            authToken = loginResponse.authToken
        }

        listener?.onBaseUrlChange(baseUrl)
    }

    fun login(loginResponse: LoginResponse) {
        user = loginResponse.user
        authToken = loginResponse.authToken
        settings.putString(KEY_USER, Json.encodeToString(loginResponse))
    }

    fun logout() {
        user = null
        authToken = null
        settings.remove(KEY_USER)
    }

    fun requireUser() = user ?: throw IllegalStateException("Not logged in")

    interface Listener {
        fun onAuthTokenChange(authToken: String?)
        fun onBaseUrlChange(baseUrl: String)
    }
}
