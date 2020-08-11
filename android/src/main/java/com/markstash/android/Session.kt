package com.markstash.android

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.ambientOf
import androidx.core.content.ContextCompat
import com.markstash.api.models.User
import com.markstash.api.sessions.LoginResponse
import com.markstash.client.api.ApiClient

class Session(context: Context) {
    companion object {
        val ambient = ambientOf<Session>()

        private const val KEY_BASE_URL = "base_url"
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

    val apiClient = ApiClient(baseUrl = baseUrl)

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
