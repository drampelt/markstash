package com.markstash.web.pages.note

import com.markstash.api.models.Note
import com.markstash.api.notes.UpdateRequest
import com.markstash.shared.js.api.notesApi
import com.markstash.shared.js.components.tagList
import com.markstash.web.components.modal
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.ButtonType
import kotlinx.html.js.onClickFunction
import react.RProps
import react.child
import react.dom.*
import react.functionalComponent
import react.useEffect
import react.useState

interface EditNoteModalProps : RProps {
    var isOpen: Boolean
    var onRequestClose: () -> Unit
    var note: Note
    var onEdit: (Note) -> Unit
}

val EditNoteModal = functionalComponent<EditNoteModalProps> { props ->
    val (tags, setTags) = useState(props.note.tags)
    val (isSaving, setIsSaving) = useState(false)
    val (error, setError) = useState<String?>(null)

    useEffect(listOf(props.isOpen)) {
        if (props.isOpen) {
            setTags(props.note.tags)
            setIsSaving(false)
            setError(null)
        }
    }

    fun handleSave() = GlobalScope.launch {
        setIsSaving(true)
        setError(null)
        try {
            val newNote = notesApi.update(props.note.id, UpdateRequest(
                content = null,
                tags = tags,
            ))
            props.onEdit(newNote)
            props.onRequestClose()
        } catch (e: Throwable) {
            setError(e.message ?: "Error saving bookmark")
        }
    }

    child(modal) {
        attrs.isOpen = props.isOpen
        attrs.onRequestClose = { if (!isSaving) props.onRequestClose() }
        attrs.contentLabel = "Edit Note"

        h3("text-lg leading-6 font-medium text-gray-900") { +"Edit Note" }
        div("mt-2") {
            label("block text-sm font-medium leading-5 text-gray-700") {
                +"Tags"
            }
            child(tagList) {
                attrs.autoFocus = false
                attrs.matchInputStyle = true
                attrs.tags = tags
                attrs.onAddTag = {
                    setTags(tags + it)
                    true
                }
                attrs.onRemoveTag = {
                    setTags(tags - it)
                    true
                }
            }
        }
        if (error != null) {
            div("mt-2 ") {
                p("text-sm leading-5 text-red-600") { +error }
            }
        }
        div("mt-5 sm:mt-4 sm:flex sm:flex-row-reverse") {
            span("flex w-full rounded-md shadow-sm sm:ml-3 sm:w-auto") {
                button(type = ButtonType.button, classes = "inline-flex justify-center w-full rounded-md border border-transparent px-4 py-2 bg-indigo-600 text-base leading-6 font-medium text-white shadow-sm hover:bg-indigo-500 focus:outline-none focus:border-indigo-700 focus:shadow-outline-indigo transition ease-in-out duration-150 sm:text-sm sm:leading-5") {
                    attrs.disabled = isSaving
                    attrs.onClickFunction = { handleSave() }
                    +(if (isSaving) "Saving..." else "Save")
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
