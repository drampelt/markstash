package com.markstash.web.pages.bookmark

import com.markstash.api.bookmarks.ShowResponse
import com.markstash.api.models.Archive
import com.markstash.shared.js.api.bookmarksApi
import com.markstash.shared.js.helpers.rawHtml
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
    val (bookmark, setBookmarkResponse) = useState<ShowResponse?>(null)
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
        bookmark != null -> {
            div("flex items-center h-16 bg-white shadow p-4") {
                div("flex-grow w-0") {
                    div("flex items-center") {
                        div("text-sm font-medium text-gray-900") { +bookmark.title }
                        div("px-1") { +"•" }
                        a(bookmark.url, classes = "text-sm text-gray-500") { +bookmark.url }
                    }
                    div("flex items-center") {
                        if (bookmark.tags.isEmpty()) {
                            span("text-sm text-gray-500") { +"No tags"}
                        } else {
                            bookmark.tags.forEach { tag ->
                                span("inline-flex items-center mr-1 px-2.5 py-0.5 rounded-full text-xs font-medium leading-4 bg-gray-200 text-gray-800 hover:bg-indigo-100") {
                                    +tag
                                }
                            }
                        }
                    }
                }
                div("flex-no-shrink flex items-center") {
                    rawHtml("w-6 h-6 ml-2 text-gray-500 hover:text-gray-700") {
                        "<svg fill=\"currentColor\" viewBox=\"0 0 20 20\"><path fill-rule=\"evenodd\" d=\"M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z\" clip-rule=\"evenodd\"></path></svg>"
                    }
                    a(href = bookmark.url, target = "_blank") {
                        rawHtml("w-6 h-6 ml-2 text-gray-500 cursor-pointer hover:text-gray-900") {
                            "<svg fill=\"currentColor\" viewBox=\"0 0 20 20\"><path d=\"M11 3a1 1 0 100 2h2.586l-6.293 6.293a1 1 0 101.414 1.414L15 6.414V9a1 1 0 102 0V4a1 1 0 00-1-1h-5z\"></path><path d=\"M5 5a2 2 0 00-2 2v8a2 2 0 002 2h8a2 2 0 002-2v-3a1 1 0 10-2 0v3H5V7h3a1 1 0 000-2H5z\"></path></svg>"
                        }
                    }
                }
            }
            renderSelectedArchive(selectedArchive)
        }
    }
}
