package com.markstash.extension.popup

import browser.browser
import com.markstash.api.bookmarks.CreateRequest
import com.markstash.api.bookmarks.UpdateRequest
import com.markstash.api.models.Bookmark
import com.markstash.shared.js.components.tagList
import com.markstash.extension.dyn
import com.markstash.shared.js.api.bookmarksApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.launch
import react.RProps
import react.child
import react.dom.*
import react.functionalComponent
import react.useEffect
import react.useState

val bookmarkForm = functionalComponent<RProps> {
    val (isLoading, setIsLoading) = useState(true)
    val (bookmark, setBookmark) = useState<Bookmark?>(null)
    val (error, setError) = useState<String?>(null)

    useEffect(listOf()) {
        GlobalScope.launch {
            val tab = browser.tabs.query(dyn {
                active = true
                currentWindow = true
            }).asDeferred().await().first()
            val url = tab.url ?: return@launch

            val newBookmark = try {
                bookmarksApi.create(CreateRequest(tab.title ?: "Untitled", url))
            } catch (e: Exception) {
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

    when {
        isLoading -> {
            p { +"Bookmarking..." }
        }
        bookmark == null || error != null -> {
            p { +(error ?: "An unknown error occurred") }
        }
        else -> {
            h2 { +bookmark.title }
            p { +bookmark.url }
            p { +"✓ Bookmark saved" }
            child(tagList) {
                attrs {
                    tags = bookmark.tags
                    onAddTag = ::handleAddTag
                    onRemoveTag = ::handleRemoveTag
                }
            }
        }
    }
}
