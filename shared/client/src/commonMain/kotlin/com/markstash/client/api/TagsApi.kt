package com.markstash.client.api

import com.markstash.api.tags.IndexResponse
import io.ktor.client.request.get

class TagsApi(apiClient: ApiClient) : BaseApi(apiClient) {
    suspend fun index(): IndexResponse {
        return client.get("$baseUrl/tags")
    }
}
