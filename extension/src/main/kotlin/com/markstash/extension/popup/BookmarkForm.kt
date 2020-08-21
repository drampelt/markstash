package com.markstash.extension.popup

import browser.browser
import com.markstash.api.bookmarks.CreateRequest
import com.markstash.api.bookmarks.UpdateRequest
import com.markstash.api.models.Bookmark
import com.markstash.shared.js.components.tagList
import com.markstash.extension.dyn
import com.markstash.shared.js.api.bookmarksApi
import com.markstash.shared.js.helpers.rawHtml
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.launch
import kotlinx.html.js.onClickFunction
import kotlinx.html.title
import react.RProps
import react.child
import react.dom.*
import react.functionalComponent
import react.useEffect
import react.useState

interface BookmarkFormProps : RProps {
    var onLogOut: () -> Unit
}

val bookmarkForm = functionalComponent<BookmarkFormProps> { props ->
    val (isLoading, setIsLoading) = useState(true)
    val (bookmark, setBookmark) = useState<Bookmark?>(null)
    val (error, setError) = useState<String?>(null)
    val (title, setTitle) = useState<String?>(null)
    val (isDeleted, setIsDeleted) = useState(false)

    useEffect(listOf()) {
        GlobalScope.launch {
            val tab = browser.tabs.query(dyn {
                active = true
                currentWindow = true
            }).asDeferred().await().first()
            setTitle(tab.title ?: "Untitled")
            val url = tab.url ?: return@launch

            val newBookmark = try {
                bookmarksApi.create(CreateRequest(tab.title ?: "Untitled", url))
            } catch (e: Throwable) {
                setError(e.message)
                setIsLoading(false)
                return@launch
            }

            setBookmark(newBookmark)
            setIsLoading(false)
        }
    }

    fun updateBookmark(bookmark: Bookmark) = GlobalScope.launch {
        bookmarksApi.update(bookmark.id, UpdateRequest(bookmark.tags))
    }

    fun handleAddTag(tag: String): Boolean {
        bookmark ?: return false
        val newBookmark = bookmark.copy(tags = bookmark.tags + tag)
        setBookmark(newBookmark)
        updateBookmark(newBookmark)
        return true
    }

    fun handleRemoveTag(tag: String): Boolean {
        bookmark ?: return false
        val newBookmark = bookmark.copy(tags = bookmark.tags - tag)
        setBookmark(newBookmark)
        updateBookmark(newBookmark)
        return true
    }

    fun handleDelete() = GlobalScope.launch {
        bookmark ?: return@launch
        bookmarksApi.delete(bookmark.id)
        setIsDeleted(true)
    }

    div("flex items-start m-2") {
        rawHtml("w-6 h-6 text-gray-700 flex-no-shrink") {
            when {
                isLoading -> "<svg fill=\"currentColor\" viewBox=\"0 0 20 20\"><path fill-rule=\"evenodd\" d=\"M4 2a1 1 0 011 1v2.101a7.002 7.002 0 0111.601 2.566 1 1 0 11-1.885.666A5.002 5.002 0 005.999 7H9a1 1 0 010 2H4a1 1 0 01-1-1V3a1 1 0 011-1zm.008 9.057a1 1 0 011.276.61A5.002 5.002 0 0014.001 13H11a1 1 0 110-2h5a1 1 0 011 1v5a1 1 0 11-2 0v-2.101a7.002 7.002 0 01-11.601-2.566 1 1 0 01.61-1.276z\" clip-rule=\"evenodd\"></path></svg>"
                error != null -> "<svg fill=\"currentColor\" viewBox=\"0 0 20 20\"><path fill-rule=\"evenodd\" d=\"M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z\" clip-rule=\"evenodd\"></path></svg>"
                isDeleted -> "<svg fill=\"currentColor\" viewBox=\"0 0 20 20\"><path fill-rule=\"evenodd\" d=\"M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z\" clip-rule=\"evenodd\"></path></svg>"
                else -> "<svg fill=\"currentColor\" viewBox=\"0 0 20 20\"><path d=\"M5 4a2 2 0 012-2h6a2 2 0 012 2v14l-5-2.5L5 18V4z\"></path></svg>"
            }
        }
        div("ml-2 w-0 flex-grow") {
            div("truncate text-sm leading-5 font-medium text-gray-900") {
                if (error == null) {
                    +(bookmark?.title ?: title ?: "Bookmarking...")
                } else {
                    +"An error occurred"
                }
            }
            if (error != null) {
                div("text-sm text-gray-500") {
                    +error
                }
            }
            if (bookmark != null) {
                div("text-sm text-gray-500 flex items-center") {
                    rawHtml("w-3 h-3") {
                        if (isDeleted) {
                            "<svg fill=\"currentColor\" viewBox=\"0 0 20 20\"><path fill-rule=\"evenodd\" d=\"M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z\" clip-rule=\"evenodd\"></path></svg>"
                        } else {
                            "<svg fill=\"currentColor\" viewBox=\"0 0 20 20\"><path fill-rule=\"evenodd\" d=\"M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z\" clip-rule=\"evenodd\"></path></svg>"
                        }
                    }
                    div("ml-1") { +(if (isDeleted) "Bookmark deleted" else "Bookmark saved") }
                }
            }
        }
    }
    if (bookmark != null && !isDeleted) {
        div("mx-2 mt-4 mb-2") {
            child(tagList) {
                attrs {
                    tags = bookmark.tags
                    onAddTag = ::handleAddTag
                    onRemoveTag = ::handleRemoveTag
                }
            }
        }
    }
    div("flex items-center px-2 border-t h-8") {
        a(classes = "cursor-pointer text-gray-500 hover:text-gray-900") {
            attrs.onClickFunction = { props.onLogOut() }
            rawHtml("w-4 h-4") {
                "<svg viewBox=\"0 0 20 20\" fill=\"currentColor\"><path fill-rule=\"evenodd\" d=\"M3 3a1 1 0 00-1 1v12a1 1 0 102 0V4a1 1 0 00-1-1zm10.293 9.293a1 1 0 001.414 1.414l3-3a1 1 0 000-1.414l-3-3a1 1 0 10-1.414 1.414L14.586 9H7a1 1 0 100 2h7.586l-1.293 1.293z\" clip-rule=\"evenodd\"></path></svg>"
            }
        }
        div("flex-grow") {}
        if (!isDeleted) {
            div("cursor-pointer text-gray-500 hover:text-gray-900") {
                attrs.title = "Delete Bookmark"
                attrs.onClickFunction = { handleDelete() }
                rawHtml("w-4 h-4") {
                    "<svg fill=\"currentColor\" viewBox=\"0 0 20 20\"><path fill-rule=\"evenodd\" d=\"M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z\" clip-rule=\"evenodd\"></path></svg>"
                }
            }
        }
    }
}
