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
import kotlinx.serialization.json.JsonBuilder
import kotlin.native.concurrent.ThreadLocal

interface ApiClient {
    val httpClient: HttpClient
    val baseUrl: String
}

// Most platforms can use singleton and then update the baseUrl/authToken as necessary
class MutableApiClient(
    override var baseUrl: String = "",
    var authToken: String? = null,
) : ApiClient {
    override val httpClient = HttpClient {
        configure()
        defaultRequest {
            authToken?.let { header("Authorization", "Bearer $it") }
        }
    }
}

// Native platforms need frozen parameters and then create a new instance of the client if they change
class FrozenApiClient(
    override val baseUrl: String = "",
    authToken: String? = null,
) : ApiClient {
    override val httpClient = HttpClient {
        configure()
        defaultRequest {
            authToken?.let { header("Authorization", "Bearer $it") }
        }
    }
}

internal fun HttpClientConfig<*>.configure() {
    val json = Json {
        ignoreUnknownKeys = true
    }

    expectSuccess = false
    install(JsonFeature) {
        serializer = KotlinxSerializer(json)
    }

    HttpResponseValidator {
        validateResponse { response ->
            val status = response.status.value
            if (status < 300) return@validateResponse

            val errorResponse = try {
                val responseString = String(response.readBytes())
                json.decodeFromString(ErrorResponse.serializer(), responseString)
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
