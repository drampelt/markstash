package com.markstash.server.workers

import io.ktor.application.Application
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class JobProcessor(private val application: Application) {
    private val jobs = Channel<Worker>(Channel.UNLIMITED)

    fun start() {
        GlobalScope.launch {
            for (job in jobs) {
                job.application = application
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
