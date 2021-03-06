package com.markstash.web.components

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.EventListenerOptions
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener
import org.w3c.dom.get
import react.RMutableRef
import react.RProps
import react.dom.*
import react.functionalComponent
import react.useEffectWithCleanup

object IFrameMousedownDispatcher {
    private var listeners = mutableListOf<EventListener>()

    fun register(listener: EventListener) {
        listeners.add(listener)
    }

    fun unregister(listener: EventListener) {
        listeners.remove(listener)
    }

    fun dispatch(e: Event) {
        listeners.forEach { it.handleEvent(e) }
    }
}

interface ArchiveIframeProps : RProps {
    var src: String
    var classes: String?
}

val ArchiveIframe = functionalComponent<ArchiveIframeProps> { props ->
    val iframeRef = js("require('react').useRef()").unsafeCast<RMutableRef<Element?>>()

    useEffectWithCleanup(listOf(iframeRef)) {
        val clickHandler = EventListener { e ->
            IFrameMousedownDispatcher.dispatch(e)
        }

        val loadListener = EventListener {
            val document = iframeRef.current.asDynamic().contentWindow.document.unsafeCast<Document>()
            document.addEventListener("click", clickHandler, EventListenerOptions(capture = true))

            val links = document.links
            for (i in 0 until links.length) {
                links[i]?.setAttribute("target", "_blank")
            }
        }

        iframeRef.current?.addEventListener("load", loadListener)

        return@useEffectWithCleanup {
            iframeRef.current?.let {
                it.removeEventListener("load", loadListener)
                it.asDynamic().contentWindow.document.unsafeCast<Document>().removeEventListener("click", clickHandler)
            }
        }
    }

    iframe(classes = props.classes) {
        ref = iframeRef
        attrs.src = props.src
    }
}
