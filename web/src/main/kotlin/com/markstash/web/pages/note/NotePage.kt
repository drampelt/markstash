package com.markstash.web.pages.note

import com.markstash.api.models.Note
import com.markstash.api.models.Resource
import com.markstash.api.notes.UpdateRequest
import com.markstash.shared.js.api.notesApi
import com.markstash.shared.js.components.resourceTag
import com.markstash.shared.js.helpers.rawHtml
import com.markstash.web.pages.index.ResourceStore
import com.markstash.web.useStore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RMutableRef
import react.RProps
import react.child
import react.dom.*
import react.functionalComponent
import react.key
import react.router.dom.RouteResultHistory
import react.router.dom.useRouteMatch
import react.useEffect
import react.useEffectWithCleanup
import react.useState

interface NotePageProps : RProps {
    var id: String
    var history: RouteResultHistory
}

val notePage = functionalComponent<NotePageProps> { props ->
    val noteId = props.id.toLong()
    val cachedNote = useStore(NoteStore, listOf(props.id)) { it.notes[noteId] }
    val cachedResource = useStore(ResourceStore, listOf(props.id)) { state ->
        state.resources.firstOrNull { it.type == Resource.Type.NOTE && it.id == noteId }
    }
    val (isLoading, setIsLoading) = useState(cachedNote == null)
    val (note, setNote) = useState(cachedNote)
    val (error, setError) = useState<String?>(null)
    val (isOptionsDropdownOpen, setIsOptionsDropdownOpen) = useState(false)
    val saveChannel = js("require('react').useRef()").unsafeCast<RMutableRef<Channel<Note>>>()
    val everythingMatch = useRouteMatch<RProps>("/everything")

    useEffect(listOf(props.id)) {
        setError(null)
    }

    useEffect(listOf(props.id, cachedNote)) {
        if (note != null && note.id == noteId) return@useEffect

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
                    val updatedNote = notesApi.update(noteToSave.id, UpdateRequest(noteToSave.content, noteToSave.tags))
                    NoteStore.updateDate(updatedNote)
                    ResourceStore.updateResourceDate(updatedNote.toResource())
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
                            child(resourceTag) {
                                attrs.tag = tag
                            }
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
                if (note != null) {
                    child(NoteOptionsDropdown) {
                        attrs.isOpen = isOptionsDropdownOpen
                        attrs.onClickOut = { setIsOptionsDropdownOpen(false) }
                        attrs.note = note
                        attrs.onDelete = { handleDelete() }
//                        attrs.onEdit = { handleEdit(it) }
                    }
                }
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
