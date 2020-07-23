package com.markstash.client.api

import com.markstash.api.notes.IndexResponse
import com.markstash.api.notes.ShowResponse
import io.ktor.client.request.get

class NotesApi(apiClient: ApiClient) : BaseApi(apiClient) {
    suspend fun index(): IndexResponse {
        return client.get("$baseUrl/notes")
    }

    suspend fun show(id: Long): ShowResponse {
        return client.get("$baseUrl/notes/$id")
    }
}
