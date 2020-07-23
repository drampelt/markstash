package com.markstash.server.controllers

import com.markstash.api.models.Resource
import com.markstash.server.auth.currentUser
import com.markstash.server.db.Database
import io.ktor.application.call
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import org.koin.ktor.ext.inject

@Location("/resources")
class Resources {
    @Location("")
    data class Index(val parent: Resources)
}

fun Route.resources() {
    val db: Database by inject()

    get<Resources.Index> {
        val resources = db.resourceQueries.indexCreatedDesc(currentUser.user.id).executeAsList().map { resource ->
            Resource(
                type = Resource.Type.valueOf(resource.resourceType.toUpperCase()),
                id = resource.resourceId,
                title = resource.title,
                excerpt = resource.excerpt,
                tags = resource.tags.split(",").filter(String::isNotBlank).toSet(),
                url = resource.url,
                createdAt = resource.createdAt,
                updatedAt = resource.updatedAt
            )
        }
        call.respond(resources)
    }
}
