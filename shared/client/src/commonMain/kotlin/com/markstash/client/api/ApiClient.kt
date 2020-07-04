package com.markstash.client.api

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer

class ApiClient(
    val baseUrl: String
) {
    val httpClient = HttpClient { configure() }

    private fun HttpClientConfig<*>.configure() {
        expectSuccess = false
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }
}
