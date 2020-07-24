package com.markstash.web.pages.note

import com.markstash.api.notes.ShowResponse
import com.markstash.shared.js.api.notesApi
import com.markstash.shared.js.helpers.rawHtml
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.id
import react.RProps
import react.child
import react.dom.*
import react.functionalComponent
import react.useEffect
import react.useState

interface NotePageProps : RProps {
    var id: Long
}

val notePage = functionalComponent<NotePageProps> { props ->
    val (isLoading, setIsLoading) = useState(true)
    val (note, setNote) = useState<ShowResponse?>(null)
    val (error, setError) = useState<String?>(null)

    useEffect(listOf()) {
        GlobalScope.launch {
            setIsLoading(true)
            setError(null)
            try {
                setNote(notesApi.show(props.id))
                setIsLoading(false)
            } catch (e: Throwable) {
                setError(e.message ?: "Error loading note")
                setIsLoading(false)
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
        note != null -> {
            div("flex items-center h-16 z-10 bg-white shadow p-4") {
                div("flex-grow w-0") {
                    div("flex items-center") {
                        div("text-sm font-medium text-gray-900") { +(note.title ?: "Untitled") }
                    }
                    div("flex items-center") {
                        if (note.tags.isEmpty()) {
                            span("text-sm text-gray-500") { +"No tags"}
                        } else {
                            note.tags.forEach { tag ->
                                span("inline-flex items-center mr-1 px-2.5 py-0.5 rounded-full text-xs font-medium leading-4 bg-gray-200 text-gray-800 hover:bg-indigo-100") {
                                    +tag
                                }
                            }
                        }
                    }
                }
                div("flex-no-shrink flex items-center") {
                    rawHtml("w-6 h-6 ml-2 text-gray-500 hover:text-gray-700") {
                        "<svg fill=\"currentColor\" viewBox=\"0 0 20 20\"><path fill-rule=\"evenodd\" d=\"M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z\" clip-rule=\"evenodd\"></path></svg>"
                    }
                }
            }
            child(noteEditor)
        }
    }
}
