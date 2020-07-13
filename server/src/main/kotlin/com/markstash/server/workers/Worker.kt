package com.markstash.server.workers

import io.ktor.application.Application

abstract class Worker {
    lateinit var application: Application
    abstract suspend fun run()
}
