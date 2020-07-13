package com.markstash.web.pages.bookmark

import com.markstash.api.bookmarks.ShowResponse
import com.markstash.api.models.Archive
import com.markstash.shared.js.api.bookmarksApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RProps
import react.dom.*
import react.functionalComponent
import react.useEffect
import react.useState

interface BookmarkPageProps : RProps {
    var id: Long
}

val bookmarkPage = functionalComponent<BookmarkPageProps> { props ->
    val (isLoading, setIsLoading) = useState(true)
    val (bookmarkResponse, setBookmarkResponse) = useState<ShowResponse?>(null)
    val (error, setError) = useState<String?>(null)
    val (selectedArchive, setSelectedArchive) = useState<Archive?>(null)

    useEffect(listOf(props.id)) {
        setSelectedArchive(null)
        setIsLoading(true)
        setError(null)
        GlobalScope.launch {
            try {
                setBookmarkResponse(bookmarksApi.show(props.id))
                setIsLoading(false)
            } catch (e: Throwable) {
                setError(e.message ?: "Error loading bookmark")
                setIsLoading(false)
            }
        }
    }

    fun RBuilder.renderArchives(archives: List<Archive>) {
        if (archives.isEmpty()) {
            div("text-gray-500") { +"No archives" }
            return
        }

        nav("-mb-px flex") {
            archives.forEach { archive ->
                val commonClasses = "cursor-pointer whitespace-no-wrap p-4 border-b-2 border-transparent font-medium text-sm"
                val currentClasses = if (selectedArchive?.key == archive.key) {
                    "text-indigo-600 border-indigo-600"
                } else {
                    "hover:text-gray-800 hover:border-gray-300 focus:outline-none focus:text-gray-900 focus:border-gray-400"
                }
                a(classes = "$commonClasses $currentClasses") {
                    attrs.key = archive.key
                    attrs.onClickFunction = { setSelectedArchive(archive) }
                    +archive.type.name.toLowerCase()
                }
            }
        }
    }

    fun RBuilder.renderSelectedArchive(archive: Archive?) {
        if (archive == null) {
            p { +"Select an archive" }
            return
        }

        iframe(classes = "w-full h-full") {
            attrs.src = "/api/archives/${archive.key}"
        }
    }

    when {
        isLoading -> {
            p { +"Loading..." }
        }
        error != null -> {
            p { +"Error: $error" }
        }
        bookmarkResponse != null -> {
            val (bookmark, archives) = bookmarkResponse
            div("border-b p-4 pb-0") {
                div("text-xl text-gray-900") { +bookmark.title }
                a(bookmark.url, classes = "text-gray-700") { +bookmark.url }
                div("text-gray-700") {
                    if (bookmark.tags.isEmpty()) {
                        span("text-gray-500") { +"No tags"}
                    } else {
                        +bookmark.tags.joinToString(", ")
                    }
                }
                renderArchives(archives)
            }
            renderSelectedArchive(selectedArchive)
        }
    }
}
