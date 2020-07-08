package com.markstash.server.workers

import com.markstash.server.db.Database
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class JobProcessor(private val db: Database) {
    private val jobs = Channel<Worker>(Channel.UNLIMITED)

    fun start() {
        GlobalScope.launch {
            for (job in jobs) {
                job.db = db
                job.run()
            }
        }
    }

    fun stop() {
        jobs.close()
    }

    suspend fun submit(job: Worker) {
        jobs.send(job)
    }
}
