package com.markstash.api.errors

class NotFoundException(errorResponse: ErrorResponse) : ServerException(404, errorResponse) {
    constructor(message: String = "Forbidden") : this(ErrorResponse.simple(message))
}
