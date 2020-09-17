package com.markstash.web.pages.bookmark

import com.markstash.shared.js.api.bookmarksApi
import com.markstash.shared.js.helpers.rawHtml
import com.markstash.web.components.modal
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.ButtonType
import kotlinx.html.js.onClickFunction
import react.RProps
import react.child
import react.dom.*
import react.functionalComponent
import react.useState

interface DeleteBookmarkModalProps : RProps {
    var isOpen: Boolean
    var onRequestClose: () -> Unit
    var onDelete: () -> Unit
    var id: Long
}

val DeleteBookmarkModal = functionalComponent<DeleteBookmarkModalProps> { props ->
    val (isSaving, setIsSaving) = useState(false)
    val (error, setError) = useState<String?>(null)

    fun handleDelete() {
        GlobalScope.launch {
            setIsSaving(true)
            try {
                bookmarksApi.delete(props.id)
                setIsSaving(false)
                props.onDelete()
                props.onRequestClose()
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
