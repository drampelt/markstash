package com.markstash.server.controllers

import com.markstash.api.errors.NotFoundException
import com.markstash.api.models.Archive
import com.markstash.server.Constants
import com.markstash.server.auth.currentUser
import com.markstash.server.db.Database
import com.markstash.server.workers.ArchiveWorker
import com.markstash.server.workers.JobProcessor
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.response.respondFile
import io.ktor.response.respondOutputStream
import io.ktor.routing.Route
import io.ktor.routing.application
import org.koin.core.qualifier.named
import org.koin.ktor.ext.inject
import java.io.File

@Location("/bookmarks/{bookmarkId}/archives")
class Archives(val bookmarkId: Long) {
    @Location("/{id}")
    data class Archive(val parent: Archives, val id: Long)
}

fun Route.archives() {
    val db: Database by inject()
    val archiveDir: String by application.inject(named(Constants.Storage.ARCHIVE_DIR))
    val jobProcessor: JobProcessor by inject()

    get<Archives.Archive> { req ->
        val bookmark = db.bookmarkQueries.findById(currentUser.user.id, req.parent.bookmarkId).executeAsOneOrNull()
            ?: throw NotFoundException()
        val archive = db.archiveQueries.findByBookmarkAndId(bookmark.id, req.id).executeAsOneOrNull()
            ?: throw NotFoundException()

        val file = File("${archiveDir}/${archive.path}")
        call.response.header(
            "Content-Security-Policy",
            "default-src 'none'; script-src 'none'; style-src data: 'unsafe-inline' https://unpkg.com/marx-css/css/marx.min.css; img-src * data:; " +
                "font-src data:; connect-src 'none'; media-src *; object-src 'none'; child-src *; frame-src *; " +
                "worker-src 'none'; frame-ancestors 'self'; form-action 'none'"
        )

        when (archive.type) {
            Archive.Type.READABILITY, Archive.Type.MONOLITH_READABILITY -> {
                call.respondOutputStream(ContentType.Text.Html) {
                    write("<html><head><title>Archive!</title><link rel=\"stylesheet\" href=\"https://unpkg.com/marx-css/css/marx.min.css\"><style>body { background: white; padding: 16px; }</style></head><body><main>".toByteArray())
                    file.inputStream().use { it.copyTo(this) }
                    write("</main></body></html>".toByteArray())
                }
            }
            else -> {
                call.respondFile(file)
            }
        }
    }

    post<Archives> { req ->
        val bookmark = db.bookmarkQueries.findById(currentUser.user.id, req.bookmarkId).executeAsOneOrNull()
            ?: throw NotFoundException()

        jobProcessor.submit(ArchiveWorker(bookmark.userId, bookmark.id))

        call.respond(HttpStatusCode.NoContent, Unit)
    }
}
