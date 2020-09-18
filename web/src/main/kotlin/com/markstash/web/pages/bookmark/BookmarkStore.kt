package com.markstash.web.pages.bookmark

import com.markstash.api.models.Bookmark
import com.markstash.web.Store

data class BookmarkStoreState(
    val bookmarks: MutableMap<Long, Bookmark> = mutableMapOf()
)

object BookmarkStore : Store<BookmarkStoreState>(BookmarkStoreState()) {
    fun update(bookmark: Bookmark) {
        state.bookmarks[bookmark.id] = bookmark
        notifyListeners()
    }

    fun delete(bookmark: Bookmark) = delete(bookmark.id)

    fun delete(id: Long) {
        state.bookmarks.remove(id)
        notifyListeners()
    }
}
