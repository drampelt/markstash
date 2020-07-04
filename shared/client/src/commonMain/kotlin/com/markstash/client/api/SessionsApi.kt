package com.markstash.client.api

import com.markstash.api.sessions.LoginRequest
import com.markstash.api.sessions.LoginResponse
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType

class SessionsApi(apiClient: ApiClient) : BaseApi(apiClient) {
    suspend fun login(request: LoginRequest): LoginResponse {
        return client.post("$baseUrl/login") {
            contentType(ContentType.Application.Json)
            body = request
        }
    }
}
