package com.markstash.web.pages.note

import com.markstash.api.models.Note
import com.markstash.web.Store

data class NoteStoreState(
    val notes: MutableMap<Long, Note> = mutableMapOf()
)

object NoteStore : Store<NoteStoreState>(NoteStoreState()) {
    fun update(note: Note) {
        state.notes[note.id] = note
        notifyListeners()
    }

    fun updateDate(note: Note) {
        state.notes[note.id] = state.notes[note.id]?.copy(updatedAt = note.updatedAt) ?: note
        notifyListeners()
    }

    fun delete(note: Note) = delete(note.id)

    fun delete(id: Long) {
        state.notes.remove(id)
        notifyListeners()
    }
}
