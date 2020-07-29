package com.markstash.web.pages.note

import muya.ChangeEvent
import muya.Muya
import muya.MuyaOptions
import org.w3c.dom.Element
import org.w3c.dom.get
import react.RMutableRef
import react.RProps
import react.dom.*
import react.functionalComponent
import react.useLayoutEffectWithCleanup
import kotlinx.browser.document
import kotlinx.browser.window

interface NoteEditorProps : RProps {
    var content: String?
    var onContentChange: ((String) -> Unit)?
}

val noteEditor = functionalComponent<NoteEditorProps> { props ->
    // useRef seems to be broken... https://github.com/JetBrains/kotlin-wrappers/issues/315
    val editorWrapper = js("require('react').useRef()").unsafeCast<RMutableRef<Element>>()
    val muya = js("require('react').useRef()").unsafeCast<RMutableRef<Muya>>()

    useLayoutEffectWithCleanup(listOf()) {
        val editorDiv = document.createElement("div")
        editorWrapper.current.appendChild(editorDiv)

        val options = js("{}").unsafeCast<MuyaOptions>().apply {
            markdown = props.content ?: ""
        }
        muya.current = Muya(editorDiv, options)
        muya.current.on("change") { changes: ChangeEvent ->
            props.onContentChange?.invoke(changes.markdown)
        }

        return@useLayoutEffectWithCleanup {
            muya.current.destroy()
            // Muya doesn't cleanup its floating divs on its own :(
            val list = document.querySelectorAll(".ag-float-wrapper, .ag-transformer")
            for (i in 0 until list.length) {
                val el = list[i]!!
                el.parentNode?.removeChild(el)
            }
        }
    }

    div("editor-wrapper w-full h-full overflow-y-auto bg-white") {
        ref = editorWrapper
    }
}
