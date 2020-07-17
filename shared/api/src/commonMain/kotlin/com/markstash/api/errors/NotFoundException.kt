package com.markstash.api.errors

class NotFoundException(errorResponse: ErrorResponse) : ServerException(404, errorResponse) {
    constructor(message: String = "Not Found") : this(ErrorResponse.simple(message))
}
