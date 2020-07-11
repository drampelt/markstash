package com.markstash.web.pages.index

import com.markstash.api.bookmarks.SearchRequest
import com.markstash.api.models.Bookmark
import com.markstash.shared.js.api.bookmarksApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.HTMLInputElement
import react.RBuilder
import react.RCleanup
import react.RProps
import react.child
import react.dom.*
import react.functionalComponent
import react.key
import react.router.dom.navLink
import react.router.dom.routeLink
import react.useEffect
import react.useEffectWithCleanup
import react.useMemo
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

private val searchInput = Channel<String>()

val bookmarkList = functionalComponent<BookmarkListProps> { props ->
    val (isLoading, setIsLoading) = useState(true)
    val (bookmarks, setBookmarks) = useState<List<Bookmark>>(emptyList())
    val (error, setError) = useState<String?>(null)
    val (search, setSearch) = useState("")

    useEffectWithCleanup(listOf()) {
        val job = GlobalScope.launch {
            searchInput.receiveAsFlow().debounce(150).collect { input ->
                try {
                    setBookmarks(if (input.isBlank()) bookmarksApi.index() else bookmarksApi.search(SearchRequest(input)).results)
                    setError(null)
                } catch (e: Throwable) {
                    setError(e.message ?: "Error loading bookmarks")
                }
            }
        }

        return@useEffectWithCleanup { job.cancel() }
    }

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

    fun RBuilder.renderSearchField() {
        div("border-b") {
            input(type = InputType.text) {
                attrs.value = search
                attrs.onChangeFunction = {
                    val value = (it.currentTarget as HTMLInputElement).value
                    setSearch(value)
                    GlobalScope.launch {
                        searchInput.send(value)
                    }
                }
            }
        }
    }

    div("flex flex-col flex-shrink-0 w-96 overflow-hidden border-r") {
        renderSearchField()
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
                div("overflow-y-auto") {
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
}
