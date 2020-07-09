package com.markstash.client.api

import com.markstash.api.models.User
import io.ktor.client.request.get

class UsersApi(apiClient: ApiClient) : BaseApi(apiClient) {
    suspend fun me(): User {
        return client.get("$baseUrl/users/me")
    }
}
