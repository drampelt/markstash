package com.markstash.web.components

import react.RProps
import react.dom.*
import react.functionalComponent
import tailwindui.Transition

interface DropdownProps : RProps {
    var isOpen: Boolean
}

val Dropdown = functionalComponent<DropdownProps> { props ->
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
            props.children()
        }
    }
}
