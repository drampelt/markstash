package com.markstash.web.pages.bookmark

import com.markstash.api.bookmarks.ShowResponse
import com.markstash.api.models.Archive
import com.markstash.api.models.Bookmark
import com.markstash.api.models.Resource
import com.markstash.shared.js.api.bookmarksApi
import com.markstash.shared.js.components.resourceTag
import com.markstash.shared.js.helpers.rawHtml
import com.markstash.web.components.ArchiveIframe
import com.markstash.web.pages.index.ResourceStore
import com.markstash.web.useStore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RProps
import react.child
import react.dom.*
import react.functionalComponent
import react.router.dom.RouteResultHistory
import react.router.dom.useRouteMatch
import react.useEffect
import react.useState

interface BookmarkPageProps : RProps {
    var id: String
    var history: RouteResultHistory
}

val bookmarkPage = functionalComponent<BookmarkPageProps> { props ->
    val bookmarkId = props.id.toLong()
    val cachedBookmark = useStore(BookmarkStore, listOf(props.id)) { it.bookmarks[bookmarkId] }
    val cachedResource = useStore(ResourceStore, listOf(props.id)) { state ->
        state.resources.firstOrNull { it.type == Resource.Type.BOOKMARK && it.id == bookmarkId }
    }
    val (isLoading, setIsLoading) = useState(cachedBookmark == null)
    val (bookmark, setBookmark) = useState(cachedBookmark)
    val (error, setError) = useState<String?>(null)
    val (selectedArchive, setSelectedArchive) = useState<Archive?>(null)
    val (isArchiveDropdownOpen, setIsArchiveDropdownOpen) = useState(false)
    val (isOptionsDropdownOpen, setIsOptionsDropdownOpen) = useState(false)
    val everythingMatch = useRouteMatch<RProps>("/everything")

    useEffect(listOf(props.id, cachedBookmark)) {
        if (bookmark != null) {
            if (bookmark.id == bookmarkId) {
                return@useEffect
            } else {
                setBookmark(null)
            }
        }

        if (cachedBookmark != null) {
            setBookmark(cachedBookmark)
            setSelectedArchive(cachedBookmark.archives?.sortedByDescending { it.createdAt }?.firstOrNull { it.type == Archive.Type.MONOLITH_READABILITY })
            return@useEffect
        }

        setSelectedArchive(null)
        setIsLoading(true)
        setError(null)
        GlobalScope.launch {
            try {
                val fullBookmark = bookmarksApi.show(bookmarkId)
                setBookmark(fullBookmark)
                setSelectedArchive(fullBookmark.archives?.sortedByDescending { it.createdAt }?.firstOrNull { it.type == Archive.Type.MONOLITH_READABILITY })
                BookmarkStore.update(fullBookmark)
                setIsLoading(false)
            } catch (e: Throwable) {
                setError(e.message ?: "Error loading bookmark")
                setIsLoading(false)
            }
        }
    }

    fun handleDelete() {
        bookmark ?: return
        val nextResource = ResourceStore.deleteResource(bookmark.toResource())
        val path = StringBuilder().apply {
            if (everythingMatch != null) append("/everything")
            if (nextResource != null) {
                append("/${nextResource.type.name.toLowerCase()}s/${nextResource.id}")
            }
        }.toString()
        props.history.push(path)
    }

    fun handleEdit(newBookmark: Bookmark) {
        setBookmark(newBookmark)
        ResourceStore.updateResource(newBookmark.toResource())
    }

    fun RBuilder.renderSelectedArchive(archive: Archive?) {
        when (archive?.status) {
            null -> {
                p { +"No archives available" }
            }
            Archive.Status.PROCESSING -> {
                p { +"Archive processing, please try again later" }
            }
            Archive.Status.FAILED -> {
                p { +"Archive processing failed" }
            }
            Archive.Status.COMPLETED -> {
                child(ArchiveIframe) {
                    key = "$bookmarkId"
                    attrs.classes = "w-full h-full"
                    attrs.src = "/api/bookmarks/${archive.bookmarkId}/archives/${archive.id}"
                }
            }
        }
    }

    fun RBuilder.renderHeader() {
        div("flex items-center bg-white shadow p-4 z-10 h-16") {
            div("flex-grow w-0") {
                div("flex items-center") {
                    div("text-sm font-medium text-gray-900 truncate") {
                        +(bookmark?.title ?: cachedResource?.title ?: "Untitled")
                    }
                    div("px-1") { +"•" }
                    a(bookmark?.url ?: cachedResource?.url, classes = "text-sm text-gray-500 truncate") {
                        +(bookmark?.url ?: cachedResource?.url ?: "")
                    }
                }
                div("flex items-center") {
                    val tags = bookmark?.tags ?: cachedResource?.tags ?: emptySet()
                    if (tags.isEmpty()) {
                        span("text-sm text-gray-500") { +"No tags"}
                    } else {
                        tags.forEach { tag ->
                            child(resourceTag) {
                                attrs.tag = tag
                            }
                        }
                    }
                }
            }
            div("flex-no-shrink flex items-center ml-4") {
                a(href = bookmark?.url ?: cachedResource?.url, target = "_blank", classes = "ml-2") {
                    rawHtml("w-6 h-6 text-gray-500 cursor-pointer hover:text-gray-900") {
                        "<svg fill=\"currentColor\" viewBox=\"0 0 20 20\"><path d=\"M11 3a1 1 0 100 2h2.586l-6.293 6.293a1 1 0 101.414 1.414L15 6.414V9a1 1 0 102 0V4a1 1 0 00-1-1h-5z\"></path><path d=\"M5 5a2 2 0 00-2 2v8a2 2 0 002 2h8a2 2 0 002-2v-3a1 1 0 10-2 0v3H5V7h3a1 1 0 000-2H5z\"></path></svg>"
                    }
                }
                div("relative") {
                    div("w-6 h-6 ml-2 text-gray-500 cursor-pointer hover:text-gray-700") {
                        attrs.onClickFunction = { setIsArchiveDropdownOpen(!isArchiveDropdownOpen) }
                        rawHtml {
                            "<svg xmlns=\"http://www.w3.org/2000/svg\" fill=\"none\" viewBox=\"0 0 24 24\" stroke=\"currentColor\"><path stroke-linecap=\"round\" stroke-linejoin=\"round\" stroke-width=\"2\" d=\"M5 8h14M5 8a2 2 0 110-4h14a2 2 0 110 4M5 8v10a2 2 0 002 2h10a2 2 0 002-2V8m-9 4h4\" /></svg>"
                        }
                    }
                    if (bookmark != null) {
                        child(ArchiveDropdown) {
                            attrs.isOpen = isArchiveDropdownOpen
                            attrs.onClickOut = { setIsArchiveDropdownOpen(false) }
                            attrs.bookmark = bookmark
                            attrs.onSelectArchive = {
                                setIsArchiveDropdownOpen(false)
                                setSelectedArchive(it)
                            }
                        }
                    }
                }
                div("relative") {
                    div("w-6 h-6 ml-2 text-gray-500 cursor-pointer hover:text-gray-700") {
                        attrs.onClickFunction = { setIsOptionsDropdownOpen(true) }
                        rawHtml {
                            "<svg xmlns=\"http://www.w3.org/2000/svg\" fill=\"none\" viewBox=\"0 0 24 24\" stroke=\"currentColor\"><path stroke-linecap=\"round\" stroke-linejoin=\"round\" stroke-width=\"2\" d=\"M12 5v.01M12 12v.01M12 19v.01M12 6a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2z\" /></svg>"
                        }
                    }
                    if (bookmark != null) {
                        child(BookmarkOptionsDropdown) {
                            attrs.isOpen = isOptionsDropdownOpen
                            attrs.onClickOut = { setIsOptionsDropdownOpen(false) }
                            attrs.bookmark = bookmark
                            attrs.onDelete = { handleDelete() }
                            attrs.onEdit = { handleEdit(it) }
                        }
                    }
                }
            }
        }
    }

    fun RBuilder.renderContent() {
        div("flex-grow bg-white") {
            when {
                isLoading -> {
                    p { +"Loading..." }
                }
                error != null -> {
                    p { +"Error: $error" }
                }
                bookmark != null -> {
                    renderSelectedArchive(selectedArchive)
                }

            }
        }
    }

    renderHeader()
    renderContent()
}
