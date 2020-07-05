package com.markstash.api.errors

open class ServerException(
    val status: Int,
    val errorResponse: ErrorResponse
) : Exception(errorResponse.message())
