package com.markstash.server.controllers

import com.markstash.api.errors.NotFoundException
import com.markstash.api.errors.ValidationException
import com.markstash.api.models.Note
import com.markstash.api.notes.CreateRequest
import com.markstash.api.notes.UpdateRequest
import com.markstash.api.notes.UpdateResponse
import com.markstash.server.auth.currentUser
import com.markstash.server.db.Database
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

@Location("/notes")
class Notes {
    @Location("")
    data class Index(val parent: Notes)

    @Location("/{id}")
    data class Note(val parent: Notes, val id: Long)
}

fun Route.notes() {
    val db: Database by inject()

    val headingRegex = Regex("^#+ ?")
    val nonAlphaRegex = Regex("[^A-Za-z0-9 .,;-]")
    val tagRegex by lazy { Regex("[a-z0-9\\-]+") }

    get<Notes.Index> {
        val notes = db.noteQueries.indexByUserId(currentUser.user.id).executeAsList()
        call.respond(notes.map { note ->
            Note(
                id = note.id,
                title = note.title,
                excerpt = note.excerpt,
                content = null,
                createdAt = note.createdAt,
                updatedAt = note.updatedAt,
                tags = note.tags.split(",").filter(String::isNotBlank).toSet()
            )
        })
    }

    post<Notes> {
        val createRequest = call.receive<CreateRequest>()
        val newTags = createRequest.tags.map { it.toLowerCase() }
        val tagsValid = newTags.all { it.matches(tagRegex) }
        if (!tagsValid) throw ValidationException("tags", "must be alphanumeric (may include dashes)")

        val note = db.transactionWithResult<Note> {
            db.noteQueries.insert(currentUser.user.id, null)
            val rowId = db.noteQueries.lastInsert().executeAsOne()

            val content = createRequest.content
            if (content != null) {
                var title: String? = null
                var excerpt: String? = null
                for (line in content.lineSequence()) {
                    if (line.isBlank()) continue
                    if (title == null) {
                        title = line.replace(headingRegex, "")
                    } else {
                        excerpt = line.replace(nonAlphaRegex, "")
                        break
                    }
                }
                db.noteQueries.update(title, excerpt, content, rowId)
            }

            newTags.forEach { tag ->
                db.tagQueries.insert(currentUser.user.id, tag)
                db.tagQueries.tagByName("note", rowId, currentUser.user.id, tag)
            }

            val dbNote = db.noteQueries.findById(currentUser.user.id, rowId).executeAsOne()
            Note(
                id = dbNote.id,
                title = dbNote.title,
                excerpt = dbNote.excerpt,
                content = dbNote.content,
                tags = dbNote.tags.split(",").filter(String::isNotBlank).toSet(),
                createdAt = dbNote.createdAt,
                updatedAt = dbNote.updatedAt
            )
        }
        call.respond(note)
    }

    patch<Notes.Note> { req ->
        val note = db.noteQueries.findById(currentUser.user.id, req.id).executeAsOneOrNull() ?: throw NotFoundException()
        val updateRequest = call.receive<UpdateRequest>()

        val newTags = updateRequest.tags.map { it.toLowerCase() }
        val tagsValid = newTags.all { it.matches(tagRegex) }
        if (!tagsValid) throw ValidationException("tags", "must be alphanumeric (may include dashes)")

        val currentTags = note.tags.split(",").toSet()
        db.transaction {
            (newTags - currentTags).forEach { newTag ->
                db.tagQueries.insert(currentUser.user.id, newTag)
                db.tagQueries.tagByName("note", note.id, currentUser.user.id, newTag)
            }

            (currentTags - newTags).forEach { oldTag ->
                db.tagQueries.untagByName("note", note.id, currentUser.user.id, oldTag)
            }
        }

        val content = updateRequest.content
        if (content != null && content != note.content) {
            var title: String? = null
            var excerpt: String? = null
            for (line in content.lineSequence()) {
                if (line.isBlank()) continue
                if (title == null) {
                    title = line.replace(headingRegex, "")
                } else {
                    excerpt = line.replace(nonAlphaRegex, "")
                    break
                }
            }
            db.noteQueries.update(title, excerpt, content, note.id)
        }

        val newNote = db.noteQueries.findById(currentUser.user.id, note.id).executeAsOne()
        call.respond(UpdateResponse(
            id = newNote.id,
            title = newNote.title,
            excerpt = newNote.excerpt,
            content = newNote.content,
            tags = newTags.toSet(),
            createdAt = newNote.createdAt,
            updatedAt = newNote.updatedAt
        ))
    }

    delete<Notes.Note> { req ->
        db.noteQueries.findById(currentUser.user.id, req.id).executeAsOneOrNull() ?: throw NotFoundException()
        db.noteQueries.deleteById(currentUser.user.id, req.id)
        call.respond(HttpStatusCode.NoContent, Unit)
    }
}
