package com.markstash.api.errors

class ForbiddenException(errorResponse: ErrorResponse) : ServerException(403, errorResponse) {
    constructor(message: String = "Forbidden") : this(ErrorResponse.simple(message))
}
