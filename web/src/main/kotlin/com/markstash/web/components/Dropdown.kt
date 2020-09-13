package com.markstash.web.components

import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.EventListenerOptions
import org.w3c.dom.Node
import org.w3c.dom.events.EventListener
import react.RMutableRef
import react.RProps
import react.dom.*
import react.functionalComponent
import react.useEffectWithCleanup
import tailwindui.Transition

interface DropdownProps : RProps {
    var isOpen: Boolean
    var onClickOut: (() -> Unit)?
}

val Dropdown = functionalComponent<DropdownProps> { props ->
    val dropdownRef = js("require('react').useRef()").unsafeCast<RMutableRef<Element?>>()
    useEffectWithCleanup(listOf(dropdownRef)) {
        val clickHandler = EventListener { e ->
            if (dropdownRef.current?.contains(e.target as? Node) == false) {
                props.onClickOut?.invoke()
                e.preventDefault()
                e.stopPropagation()
            }
        }

        IFrameMousedownDispatcher.register(clickHandler)
        document.addEventListener("click", clickHandler, EventListenerOptions(capture = true))

        return@useEffectWithCleanup {
            IFrameMousedownDispatcher.unregister(clickHandler)
            document.removeEventListener("click", clickHandler)
        }
    }

    Transition {
        attrs {
            show = props.isOpen
            enter = "transition ease-out duration-100 transform"
            enterFrom = "opacity-0 scale-95"
            enterTo = "opacity-100 scale-100"
            leave = "transition ease-in duration-75 transform"
            leaveFrom = "opacity-100 scale-100"
            leaveTo = "opacity-0 scale-95"
            className = "origin-top-right absolute right-0 mt-2 w-56 rounded-md shadow-lg"
        }

        div("rounded-md bg-white shadow-xs") {
            ref = dropdownRef

            props.children()
        }
    }
}
