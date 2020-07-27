package com.markstash.shared.js.components

import com.markstash.shared.js.helpers.rawHtml
import kotlinx.html.ButtonType
import kotlinx.html.js.onClickFunction
import org.w3c.dom.events.Event
import react.RProps
import react.dom.*
import react.functionalComponent

interface TagProps : RProps {
    var tag: String
    var isSelected: Boolean?
    var onClick: ((Event) -> Unit)?
    var onDelete: ((Event) -> Unit)?
    var classes: String?
}

val resourceTag = functionalComponent<TagProps> { props ->
    val clickClasses = if (props.onClick != null) "cursor-pointer hover:bg-indigo-100" else ""
    val bg = if (props.isSelected == true) "bg-indigo-100" else "bg-gray-200"
    span("inline-flex items-center mr-1 px-2.5 py-0.5 rounded-full text-xs font-medium leading-4 $bg text-gray-800 $clickClasses ${props.classes ?: ""}") {
        +props.tag
        props.onClick?.let { attrs.onClickFunction = it }
        props.onDelete?.let { deleteHandler ->
            button(type = ButtonType.button, classes = "flex-shrink-0 ml-1.5 inline-flex text-indigo-500 focus:outline-none focus:text-indigo-700") {
                attrs["aria-label"] = "Remove tag"
                attrs.onClickFunction = deleteHandler
                rawHtml("h-2 w-2") {
                    "<svg class=\"h-2 w-2\" stroke=\"currentColor\" fill=\"none\" viewBox=\"0 0 8 8\"><path stroke-linecap=\"round\" stroke-width=\"1.5\" d=\"M1 1l6 6m0-6L1 7\" /></svg>"
                }
            }
        }
    }
}
