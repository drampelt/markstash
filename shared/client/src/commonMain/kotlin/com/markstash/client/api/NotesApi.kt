package com.markstash.client.api

import com.markstash.api.notes.IndexResponse
import com.markstash.api.notes.SearchRequest
import com.markstash.api.notes.SearchResponse
import com.markstash.api.notes.ShowResponse
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType

class NotesApi(apiClient: ApiClient) : BaseApi(apiClient) {
    suspend fun index(): IndexResponse {
        return client.get("$baseUrl/notes")
    }

    suspend fun search(request: SearchRequest): SearchResponse {
        return client.post("$baseUrl/notes/search") {
            contentType(ContentType.Application.Json)
            body = request
        }
    }

    suspend fun show(id: Long): ShowResponse {
        return client.get("$baseUrl/notes/$id")
    }
}
