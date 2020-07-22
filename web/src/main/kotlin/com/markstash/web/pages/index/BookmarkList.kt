package com.markstash.web.pages.index

import com.markstash.api.bookmarks.SearchRequest
import com.markstash.api.models.Bookmark
import com.markstash.shared.js.api.bookmarksApi
import com.markstash.shared.js.helpers.rawHtml
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
import kotlinx.html.js.onClickFunction
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
    var onTagClick: ((String) -> Unit)?
}

private val bookmarkRow = functionalComponent<BookmarkRowProps> { props ->
    navLink<RProps>(to = "/bookmarks/${props.bookmark.id}", className = "block px-4 py-4 whitespace-no-wrap border-b border-gray-200", activeClassName = "bg-indigo-50") {
        div("flex items-center") {
            div("w-0 flex-grow") {
                div("text-sm leading-5 font-medium text-gray-900 truncate") { +props.bookmark.title }
                div("text-sm text-gray-500 truncate") { +(props.bookmark.excerpt ?: "No description") }
                div("overflow-hidden") {
                    if (props.bookmark.tags.isEmpty()) {
                        span("text-sm text-gray-500") { +"No tags" }
                    } else {
                        props.bookmark.tags.forEach { tag ->
                            span("inline-flex items-center mr-1 px-2.5 py-0.5 rounded-full text-xs font-medium leading-4 bg-gray-200 text-gray-800 hover:bg-indigo-100") {
                                +tag
                                attrs.onClickFunction = { e ->
                                    props.onTagClick?.let { callback ->
                                        e.preventDefault()
                                        callback(tag)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
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
        div("relative z-10 flex-shrink-0 flex h-16 bg-white shadow") {
            button(classes = "px-4 border-r border-gray-200 text-gray-500 focus:outline-none focus:bg-gray-100 focus:text-gray-600 md:hidden") {
                rawHtml("h-6 w-6") {
                    "<svg fill=\"currentColor\" viewBox=\"0 0 20 20\"><path fill-rule=\"evenodd\" d=\"M3 5a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zM3 10a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zM3 15a1 1 0 011-1h6a1 1 0 110 2H4a1 1 0 01-1-1z\" clip-rule=\"evenodd\"></path></svg>"
                }
            }
            div("flex-1 px-4 flex justify-between") {
                div("flex-1 flex") {
                    form(classes = "w-full flex md:ml-0") {
                        label("sr-only") {
                            attrs.htmlFor = "search"
                        }
                        div("relative w-full text-gray-400 focus-within:text-gray-600") {
                            div("absolute inset-y-0 left-0 flex items-center pointer-events-none") {
                                rawHtml("h-5 w-5") {
                                    "<svg fill=\"currentColor\" viewBox=\"0 0 20 20\"><path fill-rule=\"evenodd\" d=\"M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z\" clip-rule=\"evenodd\"></path></svg>"
                                }
                            }
                            input(type = InputType.search, classes = "block w-full h-full pl-8 pr-3 py-2 rounded-md text-gray-900 placeholder-gray-500 focus:outline-none focus:placeholder-gray-400 sm:text-sm") {
                                attrs.value = search
                                attrs.placeholder = "Search"
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
                div("flex-1 overflow-y-auto bg-white") {
                    bookmarks.forEach { bookmark ->
                        child(bookmarkRow) {
                            attrs.key = bookmark.id.toString()
                            attrs.bookmark = bookmark
                            attrs.onTagClick = { tag ->
                                setSearch(tag)
                                GlobalScope.launch {
                                    searchInput.send(tag)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
