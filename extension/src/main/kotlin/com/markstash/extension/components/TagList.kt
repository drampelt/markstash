package com.markstash.extension.components

import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onKeyDownFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import react.RProps
import react.dom.*
import react.functionalComponent
import react.useState

interface TagListProps : RProps {
    var tags: Set<String>
    var onAddTag: (String) -> Boolean
    var onRemoveTag: (String) -> Boolean
}

val tagList = functionalComponent<TagListProps> { props ->
    val (text, setText) = useState("")

    fun handleInputChange(e: Event) {
        setText((e.currentTarget as HTMLInputElement).value)
    }

    fun handleInputKeyPress(e: Event) {
        if (e.defaultPrevented) return
        val keyEvent = e.asDynamic().nativeEvent as KeyboardEvent
        when (keyEvent.code) {
            "Enter", "Comma", "Space", "Tab" -> {
                e.preventDefault()
                if (text.isNotBlank() && props.onAddTag(text.toLowerCase())) {
                    setText("")
                }
            }
            else -> {
                if (!keyEvent.key.matches("[A-Za-z0-9\\-]")) {
                    e.preventDefault()
                }
            }
        }
    }

    div {
        props.tags.forEach { tag ->
            div {
                attrs.key = tag
                span {
                    +"X"
                    attrs.onClickFunction = { props.onRemoveTag(tag) }
                }
                span { +" " }
                span { +tag }
            }
        }
        input(type = InputType.text) {
            attrs.autoFocus = true
            attrs.value = text
            attrs.onChangeFunction = { handleInputChange(it) }
            attrs.onKeyDownFunction = { handleInputKeyPress(it) }
        }
    }
}
