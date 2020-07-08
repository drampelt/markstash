package com.markstash.server.workers

import com.markstash.server.db.Database

abstract class Worker {
    lateinit var db: Database
    abstract suspend fun run()
}
