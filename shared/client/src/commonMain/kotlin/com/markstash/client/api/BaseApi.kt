package com.markstash.client.api

import io.ktor.client.HttpClient

abstract class BaseApi(val apiClient: ApiClient) {
    val client: HttpClient
        get() = apiClient.httpClient

    val baseUrl: String
        get() = apiClient.baseUrl
}
