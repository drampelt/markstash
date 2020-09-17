package com.markstash.web.components

import kotlinext.js.jsObject
import react.RProps
import react.functionalComponent
import react.modal.ReactModal

interface ModalProps : RProps {
    var isOpen: Boolean
    var onRequestClose: () -> Unit
    var contentLabel: String
}

val modal = functionalComponent<ModalProps> { props ->
    ReactModal {
        attrs.isOpen = props.isOpen
        attrs.onRequestClose = props.onRequestClose
        attrs.contentLabel = props.contentLabel
        attrs.portalClassName = ""
        attrs.overlayClassName = jsObject {
            val transitionStyle = if (props.isOpen) "ease-out duration-300" else "ease-in duration-200"
            base = "fixed inset-0 transition-colors $transitionStyle bg-gray-300 bg-opacity-0 p-4 flex items-end sm:items-center sm:justify-center z-40"
            afterOpen = if (props.isOpen) "bg-opacity-75" else ""
        }
        attrs.className = jsObject {
            val transitionStyle = if (props.isOpen) "ease-out duration-300" else "ease-in duration-200"
            base = "relative bg-white rounded-lg outline-none px-4 pt-5 pb-4 w-full shadow-xl transform transition-all $transitionStyle sm:max-w-lg sm:p-6 opacity-0 translate-y-4 sm:translate-y-0 sm:scale-95"
            afterOpen = if (props.isOpen) "opacity-100 translate-y-0 sm:scale-100" else ""
        }
        attrs.closeTimeoutMS = 200
        props.children()
    }
}
