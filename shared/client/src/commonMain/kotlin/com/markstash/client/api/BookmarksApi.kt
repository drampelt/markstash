package com.markstash.client.api

import com.markstash.api.bookmarks.CreateRequest
import com.markstash.api.bookmarks.CreateResponse
import com.markstash.api.bookmarks.ShowResponse
import com.markstash.api.bookmarks.UpdateRequest
import com.markstash.api.bookmarks.UpdateResponse
import com.markstash.api.models.Bookmark
import io.ktor.client.request.get
import io.ktor.client.request.patch
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

    suspend fun update(id: Long, request: UpdateRequest): UpdateResponse {
        return client.patch("$baseUrl/bookmarks/$id") {
            contentType(ContentType.Application.Json)
            body = request
        }
    }

    suspend fun index(): List<Bookmark> {
        return client.get("$baseUrl/bookmarks")
    }

    suspend fun show(id: Long): ShowResponse {
        return client.get("$baseUrl/bookmarks/$id")
    }
}
