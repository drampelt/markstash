package com.markstash.web.pages.note

import com.markstash.api.models.Note
import com.markstash.api.models.Resource
import com.markstash.api.notes.UpdateRequest
import com.markstash.shared.js.api.notesApi
import com.markstash.shared.js.helpers.rawHtml
import com.markstash.web.components.modal
import com.markstash.web.pages.index.ResourceStore
import com.markstash.web.useStore
import kotlinext.js.jsObject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.html.ButtonType
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RMutableRef
import react.RProps
import react.child
import react.dom.*
import react.functionalComponent
import react.key
import react.modal.ReactModal
import react.router.dom.RouteResultHistory
import react.router.dom.useRouteMatch
import react.useEffect
import react.useEffectWithCleanup
import react.useState

private interface DeleteNoteModalProps : RProps {
    var isOpen: Boolean
    var onRequestClose: () -> Unit
    var onDelete: () -> Unit
    var id: Long
}

private val deleteNoteModal = functionalComponent<DeleteNoteModalProps> { props ->
    val (isSaving, setIsSaving) = useState(false)
    val (error, setError) = useState<String?>(null)

    fun handleDelete() {
        GlobalScope.launch {
            setIsSaving(true)
            try {
                notesApi.delete(props.id)
                setIsSaving(false)
                props.onDelete()
            } catch (e: Throwable) {
                setError(e.message ?: "Could not delete note")
                setIsSaving(false)
            }
        }
    }

    child(modal) {
        attrs.isOpen = props.isOpen
        attrs.onRequestClose = { if (!isSaving) props.onRequestClose() }
        attrs.contentLabel = "Delete Note"

        div("sm:flex sm:items-start") {
            rawHtml("mx-auto flex-shrink-0 flex items-center justify-center h-12 w-12 rounded-full bg-red-100 sm:mx-0 sm:h-10 sm:w-10") {
                "<svg class=\"h-6 w-6 text-red-600\" fill=\"none\" viewBox=\"0 0 24 24\" stroke=\"currentColor\"><path stroke-linecap=\"round\" stroke-linejoin=\"round\" stroke-width=\"2\" d=\"M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z\" /></svg>"
            }
            div("mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left") {
                h3("text-lg leading-6 font-medium text-gray-900") { +"Delete Note" }
                div("mt-2") {
                    p("text-sm leading-5 text-gray-500") {
                        +"Are you sure you want to delete this note? This cannot be undone (yet)."
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

interface NotePageProps : RProps {
    var id: String
    var history: RouteResultHistory
}

val notePage = functionalComponent<NotePageProps> { props ->
    val noteId = props.id.toLong()
    val cachedNote = useStore(NoteStore, listOf(noteId)) { it.notes[noteId] }
    val cachedResource = useStore(ResourceStore, listOf(noteId)) { state ->
        state.resources.firstOrNull { it.type == Resource.Type.NOTE && it.id == noteId }
    }
    val (isLoading, setIsLoading) = useState(cachedNote == null)
    val (note, setNote) = useState(cachedNote)
    val (error, setError) = useState<String?>(null)
    val (isDeleteModalOpen, setIsDeleteModalOpen) = useState(false)
    val saveChannel = js("require('react').useRef()").unsafeCast<RMutableRef<Channel<Note>>>()
    val everythingMatch = useRouteMatch<RProps>("/everything")

    useEffect(listOf(noteId)) {
        setError(null)
    }

    useEffect(listOf(noteId, cachedNote)) {
        if (cachedNote != null) {
            setNote(cachedNote)
            return@useEffect
        }

        GlobalScope.launch {
            setNote(null)
            setIsLoading(true)
            setError(null)
            try {
                val newNote = notesApi.show(noteId)
                setNote(newNote)
                NoteStore.update(newNote)
                setIsLoading(false)
            } catch (e: Throwable) {
                setError(e.message ?: "Error loading note")
                setIsLoading(false)
            }
        }
    }

    useEffectWithCleanup(listOf()) {
        saveChannel.current = Channel()
        val job = GlobalScope.launch {
            saveChannel.current.receiveAsFlow().debounce(3000).collect { noteToSave ->
                try {
                    notesApi.update(noteToSave.id, UpdateRequest(noteToSave.content, noteToSave.tags))
                } catch (e: Throwable) {
                    // TODO: handle this
                    console.log(e)
                }
            }
        }

        return@useEffectWithCleanup  {
            job.cancel()
            saveChannel.current.close()
        }
    }

    fun handleContentChange(content: String) {
        note ?: return
        if (content != note.content) {
            val (title, excerpt) = Note.parseMetadata(content)
            val newNote = note.copy(content = content, title = title, excerpt = excerpt)
            setNote(newNote)
            GlobalScope.launch { saveChannel.current.send(newNote) }
            NoteStore.update(newNote)
            ResourceStore.updateResource(newNote.toResource())
        }
    }

    fun handleDelete() {
        note ?: return
        setIsDeleteModalOpen(false)
        NoteStore.delete(note)
        val nextResource = ResourceStore.deleteResource(note.toResource())
        val path = StringBuilder().apply {
            if (everythingMatch != null) append("/everything")
            if (nextResource != null) {
                append("/${nextResource.type.name.toLowerCase()}s/${nextResource.id}")
            }
        }.toString()
        props.history.push(path)
    }

    fun RBuilder.renderHeader() {
        div("flex items-center h-16 z-10 bg-white shadow p-4") {
            div("flex-grow w-0") {
                div("flex items-center") {
                    div("text-sm font-medium text-gray-900") {
                        +(note?.title ?: cachedResource?.title ?: "Untitled")
                    }
                }
                div("flex items-center") {
                    val tags = note?.tags ?: cachedResource?.tags ?: emptySet()
                    if (tags.isEmpty()) {
                        span("text-sm text-gray-500") { +"No tags"}
                    } else {
                        tags.forEach { tag ->
                            span("inline-flex items-center mr-1 px-2.5 py-0.5 rounded-full text-xs font-medium leading-4 bg-gray-200 text-gray-800 hover:bg-indigo-100") {
                                +tag
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
            }
            child(deleteNoteModal) {
                attrs.id = noteId
                attrs.isOpen = isDeleteModalOpen
                attrs.onRequestClose = { setIsDeleteModalOpen(false) }
                attrs.onDelete = { handleDelete() }
            }
        }
    }

    fun RBuilder.renderContent() {
        div("flex-grow bg-white overflow-y-auto") {
            when {
                isLoading -> {
                    p { +"Loading..." }
                }
                error != null -> {
                    p { +"Error: $error" }
                }
                note != null -> {
                    child(noteEditor) {
                        attrs.key = note.id.toString()
                        attrs.content = note.content
                        attrs.onContentChange = { handleContentChange(it) }
                    }
                }
            }
        }
    }

    renderHeader()
    renderContent()
}
