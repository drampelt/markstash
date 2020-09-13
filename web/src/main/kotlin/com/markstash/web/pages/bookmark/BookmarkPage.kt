package com.markstash.web.pages.bookmark

import com.markstash.api.bookmarks.ShowResponse
import com.markstash.api.models.Archive
import com.markstash.api.models.Bookmark
import com.markstash.shared.js.api.bookmarksApi
import com.markstash.shared.js.components.resourceTag
import com.markstash.shared.js.helpers.rawHtml
import com.markstash.web.components.ArchiveIframe
import com.markstash.web.components.modal
import com.markstash.web.pages.index.ResourceStore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.ButtonType
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

private interface DeleteBookmarkModalProps : RProps {
    var isOpen: Boolean
    var onRequestClose: () -> Unit
    var onDelete: () -> Unit
    var id: Long
}

private val deleteBookmarkModal = functionalComponent<DeleteBookmarkModalProps> { props ->
    val (isSaving, setIsSaving) = useState(false)
    val (error, setError) = useState<String?>(null)

    fun handleDelete() {
        GlobalScope.launch {
            setIsSaving(true)
            try {
                bookmarksApi.delete(props.id)
                setIsSaving(false)
                props.onDelete()
            } catch (e: Throwable) {
                setError(e.message ?: "Could not delete bookmark")
                setIsSaving(false)
            }
        }
    }

    child(modal) {
        attrs.isOpen = props.isOpen
        attrs.onRequestClose = { if (!isSaving) props.onRequestClose() }
        attrs.contentLabel = "Delete Bookmark"

        div("sm:flex sm:items-start") {
            rawHtml("mx-auto flex-shrink-0 flex items-center justify-center h-12 w-12 rounded-full bg-red-100 sm:mx-0 sm:h-10 sm:w-10") {
                "<svg class=\"h-6 w-6 text-red-600\" fill=\"none\" viewBox=\"0 0 24 24\" stroke=\"currentColor\"><path stroke-linecap=\"round\" stroke-linejoin=\"round\" stroke-width=\"2\" d=\"M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z\" /></svg>"
            }
            div("mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left") {
                h3("text-lg leading-6 font-medium text-gray-900") { +"Delete Bookmark" }
                div("mt-2") {
                    p("text-sm leading-5 text-gray-500") {
                        +"Are you sure you want to delete this bookmark? This cannot be undone (yet)."
                    }
                }
                if (error != null) {
                    div("mt-2") {
                        p("text-sm leading-5 text-red-600") { +error }
                    }
                }
            }
        }
        div("mt-5 sm:mt-4 sm:flex sm:flex-row-reverse") {
            span("flex w-full rounded-md shadow-sm sm:ml-3 sm:w-auto") {
                button(type = ButtonType.button, classes = "inline-flex justify-center w-full rounded-md border border-transparent px-4 py-2 bg-red-600 text-base leading-6 font-medium text-white shadow-sm hover:bg-red-500 focus:outline-none focus:border-red-700 focus:shadow-outline-red transition ease-in-out duration-150 sm:text-sm sm:leading-5") {
                    attrs.disabled = isSaving
                    attrs.onClickFunction = { handleDelete() }
                    +(if (isSaving) "Deleting..." else "Delete")
                }
            }
            span("mt-3 flex w-full rounded-md shadow-sm sm:mt-0 sm:w-auto") {
                button(type = ButtonType.button, classes = "inline-flex justify-center w-full rounded-md border border-gray-300 px-4 py-2 bg-white text-base leading-6 font-medium text-gray-700 shadow-sm hover:text-gray-500 focus:outline-none focus:border-blue-300 focus:shadow-outline-blue transition ease-in-out duration-150 sm:text-sm sm:leading-5") {
                    attrs.disabled = isSaving
                    attrs.onClickFunction = { props.onRequestClose() }
                    +"Cancel"
                }
            }
        }
    }
}

interface BookmarkPageProps : RProps {
    var id: String
    var history: RouteResultHistory
}

val bookmarkPage = functionalComponent<BookmarkPageProps> { props ->
    val bookmarkId = props.id.toLong()
    val (isLoading, setIsLoading) = useState(true)
    val (bookmark, setBookmark) = useState<ShowResponse?>(null)
    val (error, setError) = useState<String?>(null)
    val (selectedArchive, setSelectedArchive) = useState<Archive?>(null)
    val (isDeleteModalOpen, setIsDeleteModalOpen) = useState(false)
    val (isArchiveDropdownOpen, setIsArchiveDropdownOpen) = useState(false)
    val everythingMatch = useRouteMatch<RProps>("/everything")

    useEffect(listOf(props.id)) {
        setSelectedArchive(null)
        setIsLoading(true)
        setError(null)
        GlobalScope.launch {
            try {
                val fullBookmark = bookmarksApi.show(bookmarkId)
                setBookmark(fullBookmark)
                setSelectedArchive(fullBookmark.archives?.firstOrNull { it.type == Archive.Type.MONOLITH_READABILITY })
                setIsLoading(false)
            } catch (e: Throwable) {
                setError(e.message ?: "Error loading bookmark")
                setIsLoading(false)
            }
        }
    }

    fun handleDelete() {
        bookmark ?: return
        setIsDeleteModalOpen(false)
        val nextResource = ResourceStore.deleteResource(bookmark.toResource())
        val path = StringBuilder().apply {
            if (everythingMatch != null) append("/everything")
            if (nextResource != null) {
                append("/${nextResource.type.name.toLowerCase()}s/${nextResource.id}")
            }
        }.toString()
        props.history.push(path)
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
                    attrs.classes = "w-full h-full"
                    attrs.src = "/api/bookmarks/${archive.bookmarkId}/archives/${archive.id}"
                }
            }
        }
    }

    fun RBuilder.renderHeader(bookmark: Bookmark) {
        div("flex items-center bg-white shadow p-4 z-10 h-16") {
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
                            child(resourceTag) {
                                attrs.tag = tag
                            }
                        }
                    }
                }
            }
            div("flex-no-shrink flex items-center") {
                div("w-6 h-6 ml-2 text-gray-500 cursor-pointer hover:text-gray-700") {
                    attrs.onClickFunction = { setIsDeleteModalOpen(true) }
                    rawHtml {
                        "<svg fill=\"currentColor\" viewBox=\"0 0 20 20\"><path fill-rule=\"evenodd\" d=\"M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z\" clip-rule=\"evenodd\"></path></svg>"
                    }
                }
                a(href = bookmark.url, target = "_blank") {
                    rawHtml("w-6 h-6 ml-2 text-gray-500 cursor-pointer hover:text-gray-900") {
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
                div("w-6 h-6 ml-2 text-gray-500 cursor-pointer hover:text-gray-700") {
                    rawHtml {
                        "<svg xmlns=\"http://www.w3.org/2000/svg\" fill=\"none\" viewBox=\"0 0 24 24\" stroke=\"currentColor\"><path stroke-linecap=\"round\" stroke-linejoin=\"round\" stroke-width=\"2\" d=\"M12 5v.01M12 12v.01M12 19v.01M12 6a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2z\" /></svg>"
                    }
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
        bookmark != null -> {
            renderHeader(bookmark)
            renderSelectedArchive(selectedArchive)
            child(deleteBookmarkModal) {
                attrs.id = bookmarkId
                attrs.isOpen = isDeleteModalOpen
                attrs.onRequestClose = { setIsDeleteModalOpen(false) }
                attrs.onDelete = { handleDelete() }
            }
        }
    }
}
