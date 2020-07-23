package com.markstash.client.api

import com.markstash.api.resources.IndexResponse
import com.markstash.api.resources.SearchRequest
import com.markstash.api.resources.SearchResponse
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ResourcesApi(apiClient: ApiClient) : BaseApi(apiClient) {
    suspend fun index(): IndexResponse {
        return client.get("$baseUrl/resources")
    }

    suspend fun search(request: SearchRequest): SearchResponse {
        return client.post("$baseUrl/resources/search") {
            contentType(ContentType.Application.Json)
            body = request
        }
    }
}
