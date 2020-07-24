package com.markstash.web.pages.note

import muya.Muya
import org.w3c.dom.Element
import org.w3c.dom.get
import react.RMutableRef
import react.RProps
import react.dom.*
import react.functionalComponent
import react.useLayoutEffectWithCleanup
import kotlin.browser.document
import kotlin.browser.window

interface NoteEditorProps : RProps

val noteEditor = functionalComponent<NoteEditorProps> {
    // useRef seems to be broken... https://github.com/JetBrains/kotlin-wrappers/issues/315
    val editorWrapper = js("require('react').useRef()").unsafeCast<RMutableRef<Element>>()
    val muya = js("require('react').useRef()").unsafeCast<RMutableRef<Muya>>()

    useLayoutEffectWithCleanup(listOf()) {
        val editorDiv = document.createElement("div")
        editorWrapper.current.appendChild(editorDiv)

        muya.current = Muya(editorDiv)

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

    div("editor-wrapper flex-grow overflow-y-auto bg-white") {
        ref = editorWrapper
    }
}
