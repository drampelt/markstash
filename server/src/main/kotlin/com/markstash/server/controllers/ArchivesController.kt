package com.markstash.server.controllers

import com.markstash.api.errors.NotFoundException
import com.markstash.server.Constants
import com.markstash.server.db.Database
import io.ktor.application.call
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.header
import io.ktor.response.respondFile
import io.ktor.routing.Route
import io.ktor.routing.application
import org.koin.core.qualifier.named
import org.koin.ktor.ext.inject
import java.io.File

@Location("/archives")
class Archives {
    @Location("/{key}")
    data class Archive(val parent: Archives, val key: String)
}

fun Route.archives() {
    val db: Database by inject()
    val archiveDir: String by application.inject(named(Constants.Storage.ARCHIVE_DIR))

    get<Archives.Archive> { req ->
        val archive = db.archiveQueries.findByKey(req.key).executeAsOneOrNull() ?: throw NotFoundException()
        val file = File("${archiveDir}/${archive.path}")
        call.response.header(
            "Content-Security-Policy",
            "default-src 'none'; script-src 'none'; style-src data: 'unsafe-inline'; img-src * data:; " +
                "font-src data:; connect-src 'none'; media-src *; object-src 'none'; child-src *; frame-src *; " +
                "worker-src 'none'; frame-ancestors 'self'; form-action 'none'"
        )
        call.respondFile(file)
    }
}
