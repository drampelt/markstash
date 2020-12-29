package com.markstash.android

import android.content.Context
import com.markstash.api.models.User
import com.markstash.api.sessions.LoginResponse
import com.markstash.client.api.MutableApiClient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Session(context: Context) {
    companion object {
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_USER = "user"
    }

    private val sharedPreferences = context.getSharedPreferences("default", Context.MODE_PRIVATE)
    private val defaultBaseUrl = context.getString(R.string.settings_default_server_address)

    var user: User? = null
        private set

    var authToken: String? = null
        private set(value) {
            field = value
            apiClient.authToken = value
        }

    var baseUrl: String = sharedPreferences.getString(KEY_BASE_URL, null) ?: defaultBaseUrl
        set(value) {
            val newUrl = if (value.isBlank()) defaultBaseUrl else value
            field = newUrl
            apiClient.baseUrl = newUrl
            sharedPreferences.edit().apply {
                putString(KEY_BASE_URL, newUrl)
                apply()
            }
        }

    val isLoggedIn: Boolean
        get() = user != null

    val apiClient = MutableApiClient(baseUrl = baseUrl)

    init {
        sharedPreferences.getString(KEY_USER, null)?.let { userString ->
            val loginResponse = Json.decodeFromString<LoginResponse>(userString)
            user = loginResponse.user
            authToken = loginResponse.authToken
        }
    }

    fun login(loginResponse: LoginResponse) {
        user = loginResponse.user
        authToken = loginResponse.authToken
        sharedPreferences.edit().apply {
            putString(KEY_USER, Json.encodeToString(loginResponse) )
            apply()
        }
    }

    fun logout() {
        user = null
        authToken = null
        sharedPreferences.edit().apply {
            remove(KEY_USER)
            apply()
        }
    }

    fun requireUser() = user ?: throw IllegalStateException("Not logged in")
}
