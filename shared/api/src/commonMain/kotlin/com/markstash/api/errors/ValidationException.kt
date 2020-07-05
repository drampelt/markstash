package com.markstash.api.errors

class ValidationException(errorResponse: ErrorResponse) : ServerException(400, errorResponse) {
    constructor(
        target: String? = null,
        message: String = "Validation failed"
    ) : this(ErrorResponse(listOf(ErrorResponse.ErrorMessage(message, target))))
}
