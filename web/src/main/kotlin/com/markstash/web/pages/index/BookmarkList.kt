package com.markstash.web.pages.index

import com.markstash.api.models.Bookmark
import com.markstash.shared.js.api.bookmarksApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import react.RProps
import react.child
import react.dom.*
import react.functionalComponent
import react.key
import react.router.dom.navLink
import react.router.dom.routeLink
import react.useEffect
import react.useState

private interface BookmarkRowProps : RProps {
    var bookmark: Bookmark
}

private val bookmarkRow = functionalComponent<BookmarkRowProps> { props ->
    navLink<RProps>(to = "/bookmarks/${props.bookmark.id}", className = "border-b", activeClassName = "bg-gray-300") {
        div { +props.bookmark.title }
        div { +props.bookmark.url }
        div { +props.bookmark.tags.joinToString(", ") }
        div { +(props.bookmark.excerpt ?: "-") }
    }
}

interface BookmarkListProps : RProps

val bookmarkList = functionalComponent<BookmarkListProps> { props ->
    val (isLoading, setIsLoading) = useState(true)
    val (bookmarks, setBookmarks) = useState<List<Bookmark>>(emptyList())
    val (error, setError) = useState<String?>(null)

    useEffect(listOf()) {
        GlobalScope.launch {
            try {
                setBookmarks(bookmarksApi.index())
                setIsLoading(false)
            } catch (e: Throwable) {
                setError(e.message ?: "Error loading bookmarks")
                setIsLoading(false)
            }
        }
    }

    div("flex flex-col flex-shrink-0 w-96 overflow-y-auto border-r") {
        when {
            isLoading -> {
                p { +"Loading..." }
            }
            error != null -> {
                p { +"Error: $error" }
            }
            bookmarks.isEmpty() -> {
                p { +"No bookmarks found" }
            }
            else -> {
                bookmarks.forEach { bookmark ->
                    child(bookmarkRow) {
                        attrs.key = bookmark.id.toString()
                        attrs.bookmark = bookmark
                    }
                }
            }
        }
    }
}
