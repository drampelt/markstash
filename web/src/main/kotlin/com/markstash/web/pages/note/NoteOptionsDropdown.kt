package com.markstash.web.pages.note

import com.markstash.api.models.Note
import com.markstash.shared.js.helpers.rawHtml
import com.markstash.web.components.Dropdown
import kotlinx.html.js.onClickFunction
import react.RProps
import react.child
import react.dom.*
import react.functionalComponent
import react.useState

interface NoteOptionsDropdownProps : RProps {
    var isOpen: Boolean
    var note: Note
    var onClickOut: () -> Unit
    var onDelete: () -> Unit
    var onEdit: (Note) -> Unit
}

val NoteOptionsDropdown = functionalComponent<NoteOptionsDropdownProps> { props ->
    val (isDeleteModalOpen, setIsDeleteModalOpen) = useState(false)
    val (isEditModalOpen, setIsEditModalOpen) = useState(false)

    child(Dropdown) {
        attrs.isOpen = props.isOpen
        attrs.onClickOut = props.onClickOut

        div("py-1") {
            a(href = null, classes = "group flex items-center px-4 py-2 cursor-pointer text-sm leading-5 text-gray-700 hover:bg-gray-100 hover:text-gray-900 focus:outline-none focus:bg-gray-100 focus:text-gray-900") {
                rawHtml("mr-3 h-5 w-5 text-gray-400 group-hover:text-gray-500 group-focus:text-gray-500") {
                    "<svg xmlns=\"http://www.w3.org/2000/svg\" fill=\"none\" viewBox=\"0 0 24 24\" stroke=\"currentColor\"><path stroke-linecap=\"round\" stroke-linejoin=\"round\" stroke-width=\"2\" d=\"M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z\" /></svg>"
                }
                attrs.onClickFunction = {
                    props.onClickOut()
                    setIsEditModalOpen(true)
                }
                +"Edit Note..."
            }
            a(href = null, classes = "group flex items-center px-4 py-2 cursor-pointer text-sm leading-5 text-gray-700 hover:bg-gray-100 hover:text-gray-900 focus:outline-none focus:bg-gray-100 focus:text-gray-900") {
                rawHtml("mr-3 h-5 w-5 text-gray-400 group-hover:text-gray-500 group-focus:text-gray-500") {
                    "<svg xmlns=\"http://www.w3.org/2000/svg\" fill=\"none\" viewBox=\"0 0 24 24\" stroke=\"currentColor\"><path stroke-linecap=\"round\" stroke-linejoin=\"round\" stroke-width=\"2\" d=\"M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16\" /></svg>"
                }
                attrs.onClickFunction = {
                    props.onClickOut()
                    setIsDeleteModalOpen(true)
                }
                +"Delete Note..."
            }
        }
    }

    child(DeleteNoteModal) {
        attrs.id = props.note.id
        attrs.isOpen = isDeleteModalOpen
        attrs.onRequestClose = { setIsDeleteModalOpen(false) }
        attrs.onDelete = { props.onDelete() }
    }
//
//    child(EditNoteModal) {
//        attrs.note = props.note
//        attrs.isOpen = isEditModalOpen
//        attrs.onRequestClose = { setIsEditModalOpen(false) }
//        attrs.onEdit = { props.onEdit(it) }
//    }
}
