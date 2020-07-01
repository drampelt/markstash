package com.markstash.server

import com.markstash.server.db.Database
import com.markstash.server.db.User
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.CallLogging
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.dsl.module
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.get

fun Application.main() {
    install(CallLogging)
    install(Routing) {
        get("/") {
            call.respondText("Hello, World!", ContentType.Text.Html)
        }
    }

    install(Koin) {
        modules(module {
            single<SqlDriver> { JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY) }
            single { Database(get()) }
        })
    }

    GlobalScope.launch(Dispatchers.IO) {
        Database.Schema.create(get())
    }

    Runtime.getRuntime().addShutdownHook(Thread {
        log.info("Shutting down...")
        val sqlDriver = get<SqlDriver>()
        sqlDriver.close()
        log.info("Goodbye")
    })
}
