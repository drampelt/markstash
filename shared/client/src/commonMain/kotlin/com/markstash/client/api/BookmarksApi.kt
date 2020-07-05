package com.markstash.client.api

import com.markstash.api.bookmarks.CreateRequest
import com.markstash.api.bookmarks.CreateResponse
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType

class BookmarksApi(apiClient: ApiClient) : BaseApi(apiClient) {
    suspend fun create(request: CreateRequest): CreateResponse {
        return client.post("$baseUrl/bookmarks") {
            contentType(ContentType.Application.Json)
            body = request
        }
    }
}
