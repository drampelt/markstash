package com.markstash.server.controllers

import com.markstash.api.models.Resource
import com.markstash.api.resources.SearchRequest
import com.markstash.api.resources.SearchResponse
import com.markstash.server.auth.currentUser
import com.markstash.server.db.Database
import io.ktor.application.call
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import kotlinx.datetime.toInstant
import org.koin.ktor.ext.inject

@Location("/resources")
class Resources {
    @Location("")
    data class Index(val parent: Resources)

    @Location("/search")
    data class Search(val parent: Resources)
}

fun Route.resources() {
    val db: Database by inject()

    get<Resources.Index> {
        val resources = db.resourceQueries.index(currentUser.user.id).executeAsList().map { resource ->
            Resource(
                type = Resource.Type.valueOf(resource.resourceType.toUpperCase()),
                id = resource.resourceId,
                title = resource.title,
                excerpt = resource.excerpt,
                tags = resource.tags.split(",").filter(String::isNotBlank).toSet(),
                url = resource.url,
                createdAt = resource.createdAt.toInstant(),
                updatedAt = resource.updatedAt.toInstant()
            )
        }
        call.respond(resources)
    }

    post<Resources.Search> {
        val searchRequest = call.receive<SearchRequest>()
        val resources = db.resourceQueries.search(""""${searchRequest.query}"""", currentUser.user.id).executeAsList().map { resource ->
            Resource(
                type = Resource.Type.valueOf(resource.resourceType.toUpperCase()),
                id = resource.resourceId!!,
                title = resource.title,
                excerpt = resource.snippet,
                tags = resource.tags!!.split(",").filter(String::isNotBlank).toSet(),
                url = resource.url,
                createdAt = resource.createdAt!!.toInstant(),
                updatedAt = resource.updatedAt!!.toInstant()
            )
        }
        call.respond(SearchResponse(resources))
    }
}
