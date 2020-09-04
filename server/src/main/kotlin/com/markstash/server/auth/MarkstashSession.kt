package com.markstash.server.auth

import io.ktor.sessions.SessionSerializer

data class MarkstashSession(
    val authToken: String
) {
    companion object {
        val serializer = object : SessionSerializer<MarkstashSession> {
            override fun deserialize(text: String): MarkstashSession {
                return MarkstashSession(text)
            }

            override fun serialize(session: MarkstashSession): String {
                return session.authToken
            }
        }
    }
}
