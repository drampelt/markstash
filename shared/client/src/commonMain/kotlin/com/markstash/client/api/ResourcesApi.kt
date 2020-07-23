package com.markstash.client.api

import com.markstash.api.resources.IndexResponse
import io.ktor.client.request.get

class ResourcesApi(apiClient: ApiClient) : BaseApi(apiClient) {
    suspend fun index(): IndexResponse {
        return client.get("$baseUrl/resources")
    }
}
