package com.markstash.server.auth

import java.security.SecureRandom

object ApiKeyGenerator {
    private val charPool: List<Char> = ('a'..'f') + ('0'..'9')

    fun generate(): String {
        val random = SecureRandom()
        return (1..32)
            .map { random.nextInt(charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }
}
