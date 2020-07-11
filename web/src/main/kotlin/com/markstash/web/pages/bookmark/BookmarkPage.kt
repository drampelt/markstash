package com.markstash.web.pages.bookmark

import com.markstash.api.bookmarks.ShowResponse
import com.markstash.api.models.Archive
import com.markstash.shared.js.api.bookmarksApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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

    useEffect(listOf(props.id)) {
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
        val archive = archives.firstOrNull { it.type == Archive.Type.READABILITY }
        val data = archive?.data
        if (archive == null) {
            p { +"No archive found :(" }
        } else if (data == null) {
            p { +"Archive is empty :(" }
        } else {
            div("overflow-y-auto bg-white") {
                div("readability") {
                    attrs["dangerouslySetInnerHTML"] = InnerHTML(data)
                }
            }
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
            div("border-b p-4") {
                div("text-xl text-gray-900") { +bookmark.title }
                a(bookmark.url, classes = "text-gray-700") { +bookmark.url }
                div("text-gray-700") {
                    if (bookmark.tags.isEmpty()) {
                        span("text-gray-500") { +"No tags"}
                    } else {
                        +bookmark.tags.joinToString(", ")
                    }
                }
            }
            renderArchives(archives)
        }
    }
}
