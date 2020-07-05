package com.markstash.server.controllers

import com.markstash.api.bookmarks.CreateRequest
import com.markstash.api.bookmarks.CreateResponse
import com.markstash.api.errors.NotFoundException
import com.markstash.api.models.Bookmark
import com.markstash.server.auth.currentUser
import com.markstash.server.db.Database
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.locations.delete
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import org.koin.ktor.ext.inject
import com.markstash.server.db.Bookmark as DbBookmark

@Location("/bookmarks")
class Bookmarks {
    @Location("")
    data class Index(val parent: Bookmarks)

    @Location("/{id}")
    data class Bookmark(val parent: Bookmarks, val id: Long)
}

fun Route.bookmarks() {
    val db: Database by inject()

    get<Bookmarks.Index> {
        val bookmarks = db.bookmarkQueries.findByUserId(currentUser.user.id).executeAsList()
        call.respond(bookmarks.map { bookmark ->
            Bookmark(
                id = bookmark.id,
                title = bookmark.title,
                url = bookmark.url
            )
        })
    }

    post<Bookmarks> {
        val req = call.receive<CreateRequest>()
        val bookmark = db.transactionWithResult<DbBookmark> {
            db.bookmarkQueries.insert(
                userId = currentUser.user.id,
                title = req.title,
                url = req.url
            )
            val rowId = db.bookmarkQueries.lastInsert().executeAsOne()
            db.bookmarkQueries.findById(currentUser.user.id, rowId).executeAsOne()
        }
        call.respond(CreateResponse(
            id = bookmark.id,
            title = bookmark.title,
            url = bookmark.url
        ))
    }

    get<Bookmarks.Bookmark> { req ->
        val bookmark = db.bookmarkQueries.findById(currentUser.user.id, req.id).executeAsOneOrNull() ?: throw NotFoundException()
        call.respond(Bookmark(
            id = bookmark.id,
            title = bookmark.title,
            url = bookmark.url
        ))
    }

    delete<Bookmarks.Bookmark> { req ->
        db.bookmarkQueries.findById(currentUser.user.id, req.id).executeAsOneOrNull() ?: throw NotFoundException()
        db.bookmarkQueries.deleteById(currentUser.user.id, req.id)
        call.response.status(HttpStatusCode.NoContent)
        call.respond(HttpStatusCode.NoContent, Unit)
    }
}
