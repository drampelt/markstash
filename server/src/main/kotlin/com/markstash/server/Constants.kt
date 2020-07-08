package com.markstash.server

object Constants {
    object Jwt {
        const val ISSUER = "jwt.issuer"
        const val AUDIENCE = "jwt.audience"
        const val REALM = "jwt.realm"
        const val SECRET = "jwt.secret"
        const val ALGORITHM = "jwt.algorithm"
    }

    val JOB_CHANNEL = "job_channel"
}
