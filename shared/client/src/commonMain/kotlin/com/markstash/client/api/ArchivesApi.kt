package com.markstash.client.api

import com.markstash.api.archives.FetchRequest
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ArchivesApi(apiClient: ApiClient) : BaseApi(apiClient) {
    suspend fun fetch(bookmarkId: Long, request: FetchRequest) {
        return client.post("$baseUrl/bookmarks/$bookmarkId/archives") {
            contentType(ContentType.Application.Json)
            body = request
        }
    }
}
