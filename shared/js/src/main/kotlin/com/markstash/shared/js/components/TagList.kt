package com.markstash.shared.js.components

import com.markstash.api.tags.IndexResponse
import com.markstash.shared.js.api.tagsApi
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.InputType
import kotlinx.html.js.onBlurFunction
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onFocusFunction
import kotlinx.html.js.onKeyDownFunction
import kotlinx.html.js.onMouseOutFunction
import kotlinx.html.js.onMouseOverFunction
import org.w3c.dom.Element
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import react.RBuilder
import react.RMutableRef
import react.RProps
import react.child
import react.dom.*
import react.functionalComponent
import react.useLayoutEffect
import react.useState
import kotlin.math.max
import kotlin.math.min

interface TagListProps : RProps {
    var tags: Set<String>
    var onAddTag: (String) -> Boolean
    var onRemoveTag: (String) -> Boolean
    var showSuggestionsInline: Boolean?
    var autoFocus: Boolean?
    var matchInputStyle: Boolean?
}

val tagList = functionalComponent<TagListProps> { props ->
    val (text, setText) = useState("")
    val (isLastSelected, setIsLastSelected) = useState(false)
    val (suggestionList, setSuggestionList) = useState<List<IndexResponse.Tag>?>(null)
    val (showSuggestionList, setShowSuggestionList) = useState(false)
    val (isFocused, setIsFocused) = useState(false)
    val (suggestionListHovered, setSuggestionListHovered) = useState(false)
    val (selectedSuggestionIndex, setSelectedSuggestionIndex) = useState(-1)
    val selectedSuggestionRef = js("require('react').useRef()").unsafeCast<RMutableRef<Element?>>()
    val inputRef = js("require('react').useRef()").unsafeCast<RMutableRef<HTMLInputElement?>>()

    val useMemo = js("require('react').useMemo")
    val filteredTagList = useMemo({
        return@useMemo suggestionList?.filter { tag ->
            tag.name !in props.tags && (text.isBlank() || tag.name.contains(text, ignoreCase = true))
        }
    }, arrayOf(text, suggestionList, props.tags)).unsafeCast<List<IndexResponse.Tag>?>()

    useLayoutEffect(listOf(selectedSuggestionIndex)) {
        selectedSuggestionRef.current?.scrollIntoView(js("{block: 'nearest'}"))
    }

    fun handleFocus() {
        setIsFocused(true)
        setShowSuggestionList(true)
        if (suggestionList == null) {
            GlobalScope.launch {
                try {
                    setSuggestionList(tagsApi.index().tags)
                } catch (e: Throwable) {
                    // TODO: handle this
                    console.log(e)
                }
            }
        }
    }

    fun handleBlur() {
        setIsFocused(false)
        setIsLastSelected(false)

        if (!suggestionListHovered) {
            setShowSuggestionList(false)
        }
    }

    fun handleInputChange(e: Event) {
        setText((e.currentTarget as HTMLInputElement).value)
    }

    fun requestFocus() {
        window.setTimeout({
            inputRef.current?.focus()
        }, 1)
    }

    fun handleInputKeyPress(e: Event) {
        if (e.defaultPrevented) return
        val keyEvent = e.asDynamic().nativeEvent as KeyboardEvent
        when (keyEvent.key) {
            "Enter", ",", " ", "Tab" -> {
                e.preventDefault()
                if (selectedSuggestionIndex >= 0 && filteredTagList != null && selectedSuggestionIndex < filteredTagList.size) {
                    if (props.onAddTag(filteredTagList[selectedSuggestionIndex].name)) {
                        setText("")
                        setSelectedSuggestionIndex(-1)
                        requestFocus()
                    }
                } else if (text.isNotBlank() && props.onAddTag(text.toLowerCase())) {
                    setText("")
                    requestFocus()
                }
            }
            "Backspace" -> {
                if (text.isEmpty()) {
                    e.preventDefault()
                    if (isLastSelected) {
                        setIsLastSelected(false)
                        props.onRemoveTag(props.tags.last())
                    } else {
                        setIsLastSelected(true)
                    }
                    requestFocus()
                }
            }
            "ArrowDown" -> {
                e.preventDefault()
                setSelectedSuggestionIndex(min(selectedSuggestionIndex + 1, (filteredTagList?.size ?: 0) - 1))
            }
            "ArrowUp" -> {
                e.preventDefault()
                setSelectedSuggestionIndex(max(selectedSuggestionIndex - 1, -1))
            }
            else -> {
                setIsLastSelected(false)
                if (!keyEvent.key.matches("[A-Za-z0-9\\-]")) {
                    e.preventDefault()
                }
            }
        }
    }

    fun RBuilder.renderTagList() {
        if (filteredTagList == null || filteredTagList.isEmpty() || !showSuggestionList) return
        val sizeClasses = if (props.showSuggestionsInline == true) "w-56 mt-2" else "left-0 right-0 mb-2"
        div("absolute $sizeClasses max-h-64 rounded-md shadow-lg") {
            div("rounded-md bg-white shadow-xs max-h-64 overflow-y-auto py-1") {
                attrs.onMouseOverFunction = { setSuggestionListHovered(true) }
                attrs.onMouseOutFunction = {
                    setSuggestionListHovered(false)
                    if (!isFocused) setShowSuggestionList(false)
                }
                filteredTagList.forEachIndexed { index, tag ->
                    val selectedClass = if (index == selectedSuggestionIndex) "bg-gray-100" else ""
                    div("flex items-center px-4 py-2 text-sm leading-5 text-gray-700 cursor-pointer $selectedClass hover:bg-gray-100 hover:text-gray-900 focus:outline-none focus:bg-gray-100 focus:text-gray-900") {
                        if (index == selectedSuggestionIndex) ref = selectedSuggestionRef
                        attrs.key = tag.name
                        attrs.onClickFunction = {
                            it.stopPropagation()
                            props.onAddTag(tag.name)
                            requestFocus()
                        }
                        div("flex-grow truncate") {
                            +tag.name
                        }
                        div("ml-2 text-gray-500") {
                            +tag.count.toString()
                        }
                    }
                }
            }
        }
    }

    val wrapperClasses = if (props.matchInputStyle == true) "mt-1 rounded-md shadow-sm" else ""
    val selectedBorderClasses = if (isFocused) "border-blue-300 shadow-outline" else ""
    val borderClasses = if (props.matchInputStyle == true) "border rounded-md py-2 px-3 w-full transition duration-150 ease-in-out sm:text-sm sm:leading-5 $selectedBorderClasses" else ""
    div(wrapperClasses) {
        div("flex items-center flex-wrap relative $borderClasses") {
            attrs.onClickFunction = { inputRef.current?.focus() }
            props.tags.forEachIndexed { index, tag ->
                attrs.key = tag
                child(resourceTag) {
                    attrs.tag = tag
                    attrs.onDelete = {
                        it.stopPropagation()
                        props.onRemoveTag(tag)
                        requestFocus()
                    }
                    attrs.isSelected = isLastSelected && index == props.tags.size - 1
                    attrs.classes = "mb-1"
                }
            }
            div("${if (props.tags.isEmpty()) "" else "ml-1"} ${if (props.showSuggestionsInline == true) "relative" else ""}") {
                input(type = InputType.text, classes = "bg-none ml-1 focus:outline-none py-1 text-xs") {
                    ref = inputRef
                    attrs.autoFocus = props.autoFocus ?: true
                    attrs.value = text
                    if (props.tags.isEmpty()) attrs.placeholder = "Add tags..."
                    attrs.onChangeFunction = { handleInputChange(it) }
                    attrs.onKeyDownFunction = { handleInputKeyPress(it) }
                    attrs.onBlurFunction = { handleBlur() }
                    attrs.onFocusFunction = { handleFocus() }
                }
                renderTagList()
            }
        }
    }
}
