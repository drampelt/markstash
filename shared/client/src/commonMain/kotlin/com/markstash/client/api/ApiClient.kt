package com.markstash.client.api

import com.markstash.api.errors.ErrorResponse
import com.markstash.api.errors.ForbiddenException
import com.markstash.api.errors.NotFoundException
import com.markstash.api.errors.ServerException
import com.markstash.api.errors.ValidationException
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.features.HttpResponseValidator
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.header
import io.ktor.client.statement.readBytes
import io.ktor.utils.io.core.String
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

class ApiClient(
    var baseUrl: String,
    var authToken: String? = null
) {
    val httpClient = HttpClient { configure() }
    val json = Json(JsonConfiguration.Stable)

    private fun HttpClientConfig<*>.configure() {
        expectSuccess = false
        install(JsonFeature) {
            serializer = KotlinxSerializer(json)
        }

        defaultRequest {
            authToken?.let { header("Authorization", "Bearer $it") }
        }

        HttpResponseValidator {
            validateResponse { response ->
                val status = response.status.value
                if (status < 300) return@validateResponse

                val errorResponse = try {
                    val responseString = String(response.readBytes())
                    json.parse(ErrorResponse.serializer(), responseString)
                } catch (e: Throwable) {
                    when (status) {
                        400 -> throw ValidationException()
                        403 -> throw ForbiddenException()
                        404 -> throw NotFoundException()
                        else -> throw ServerException(status, ErrorResponse.simple("An unknown error occurred"))
                    }
                }

                when (status) {
                    400 -> throw ValidationException(errorResponse)
                    403 -> throw ForbiddenException(errorResponse)
                    404 -> throw NotFoundException(errorResponse)
                    else -> throw ServerException(status, errorResponse)
                }
            }
        }
    }
}
