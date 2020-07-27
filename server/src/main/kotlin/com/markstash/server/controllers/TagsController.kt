package com.markstash.server.controllers

import com.markstash.api.tags.IndexResponse
import com.markstash.server.auth.currentUser
import com.markstash.server.db.Database
import io.ktor.application.call
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import org.koin.ktor.ext.inject

@Location("/tags")
class Tags {
    @Location("")
    data class Index(val parent: Tags)
}

fun Route.tags() {
    val db: Database by inject()

    get<Tags.Index> {
        val tags = db.tagQueries.indexByUser(currentUser.user.id).executeAsList().map { tag ->
            IndexResponse.Tag(name = tag.name, count = tag.count)
        }
        call.respond(IndexResponse(tags))
    }
}
