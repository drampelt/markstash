package com.markstash.server.controllers

import com.markstash.api.bookmarks.CreateRequest
import com.markstash.api.bookmarks.CreateResponse
import com.markstash.api.bookmarks.ShowResponse
import com.markstash.api.bookmarks.UpdateRequest
import com.markstash.api.errors.NotFoundException
import com.markstash.api.errors.ValidationException
import com.markstash.api.models.Archive
import com.markstash.api.models.Bookmark
import com.markstash.server.auth.currentUser
import com.markstash.server.db.Database
import com.markstash.server.workers.ArchiveWorker
import com.markstash.server.workers.JobProcessor
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.locations.delete
import io.ktor.locations.get
import io.ktor.locations.patch
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import org.koin.ktor.ext.inject

@Location("/bookmarks")
class Bookmarks {
    @Location("")
    data class Index(val parent: Bookmarks)

    @Location("/{id}")
    data class Bookmark(val parent: Bookmarks, val id: Long)
}

fun Route.bookmarks() {
    val db: Database by inject()
    val jobProcessor: JobProcessor by inject()

    val tagRegex by lazy { Regex("[a-z0-9\\-]+") }

    get<Bookmarks.Index> {
        val bookmarks = db.bookmarkQueries.findByUserId(currentUser.user.id).executeAsList()
        call.respond(bookmarks.map { bookmark ->
            Bookmark(
                id = bookmark.id,
                title = bookmark.title,
                url = bookmark.url,
                excerpt = bookmark.excerpt,
                author = bookmark.author,
                tags = bookmark.tags.split(",").filter(String::isNotBlank).toSet()
            )
        })
    }

    post<Bookmarks> {
        val req = call.receive<CreateRequest>()
        // TODO: normalize URLs somehow
        var isNew = false
        val bookmark = db.bookmarkQueries.findByUrl(currentUser.user.id, req.url).executeAsOneOrNull()
            ?: db.transactionWithResult {
                isNew = true
                db.bookmarkQueries.insert(
                    userId = currentUser.user.id,
                    title = req.title,
                    url = req.url
                )
                val rowId = db.bookmarkQueries.lastInsert().executeAsOne()
                db.bookmarkQueries.findById(currentUser.user.id, rowId).executeAsOne()
            }

        if (isNew) jobProcessor.submit(ArchiveWorker(bookmark.userId, bookmark.id))

        call.respond(CreateResponse(
            id = bookmark.id,
            title = bookmark.title,
            url = bookmark.url,
            excerpt = bookmark.excerpt,
            author = bookmark.author,
            tags = bookmark.tags.split(",").filter(String::isNotBlank).toSet()
        ))
    }

    get<Bookmarks.Bookmark> { req ->
        val bookmark = db.bookmarkQueries.findById(currentUser.user.id, req.id).executeAsOneOrNull()
            ?: throw NotFoundException()
        val archives = db.archiveQueries.findByBookmark(bookmark.id).executeAsList()
        call.respond(ShowResponse(
            bookmark = Bookmark(
                id = bookmark.id,
                title = bookmark.title,
                url = bookmark.url,
                excerpt = bookmark.excerpt,
                author = bookmark.author,
                tags = bookmark.tags.split(",").filter(String::isNotBlank).toSet()
            ),
            archives = archives.map { archive ->
                Archive(
                    id = archive.id,
                    bookmarkId = archive.bookmarkId,
                    type = archive.type,
                    status = archive.status,
                    path = archive.path,
                    data = archive.data
                )
            }
        ))
    }

    patch<Bookmarks.Bookmark> { req ->
        val bookmark = db.bookmarkQueries.findById(currentUser.user.id, req.id).executeAsOneOrNull()
            ?: throw NotFoundException()
        val updateRequest = call.receive<UpdateRequest>()
        val newTags = updateRequest.tags.map { it.toLowerCase() }
        val tagsValid = newTags.all { it.matches(tagRegex) }
        if (!tagsValid) throw ValidationException("tags", "must be alphanumeric (may include dashes)")

        val currentTags = bookmark.tags.split(",").toSet()
        db.transaction {
            (newTags - currentTags).forEach { newTag ->
                db.tagQueries.insert(currentUser.user.id, newTag)
                db.tagQueries.tagByName("bookmark", bookmark.id, currentUser.user.id, newTag)
            }

            (currentTags - newTags).forEach { oldTag ->
                db.tagQueries.untagByName("bookmark", bookmark.id, currentUser.user.id, oldTag)
            }
        }
        call.respond(Bookmark(
            id = bookmark.id,
            title = bookmark.title,
            url = bookmark.url,
            excerpt = bookmark.excerpt,
            author = bookmark.author,
            tags = newTags.toSet()
        ))
    }

    delete<Bookmarks.Bookmark> { req ->
        db.bookmarkQueries.findById(currentUser.user.id, req.id).executeAsOneOrNull() ?: throw NotFoundException()
        db.bookmarkQueries.deleteById(currentUser.user.id, req.id)
        call.response.status(HttpStatusCode.NoContent)
        call.respond(HttpStatusCode.NoContent, Unit)
    }
}
