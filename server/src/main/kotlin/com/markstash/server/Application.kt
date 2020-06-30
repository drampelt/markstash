package com.markstash.server

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import org.koin.dsl.module
import org.koin.ktor.ext.Koin

fun Application.main() {
    install(CallLogging)
    install(Routing) {
        get("/") {
            call.respondText("Hello, World!", ContentType.Text.Html)
        }
    }

    install(Koin) {
        modules(module {

        })
    }
}
