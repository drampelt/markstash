package com.markstash.shared.js.icons

import com.markstash.shared.js.helpers.rawHtml
import react.RBuilder
import react.RProps
import react.child
import react.functionalComponent

private interface LogoProps : RProps {
    var classes: String?
}

private val logo = functionalComponent<LogoProps> { props ->
    rawHtml(props.classes) {
        "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 24 24\"><path d=\"M6 12V2h6v10L9 9l-3 3zM24 10l-2-2-8 8-.5 2.5L16 18l8-8z\" fill=\"currentColor\"/><path fill-rule=\"evenodd\" clip-rule=\"evenodd\" d=\"M3 5a2 2 0 011.5-1.94V12a1.5 1.5 0 002.56 1.06L9 11.12l1.94 1.94A1.5 1.5 0 0013.5 12V3H19a2 2 0 012 2v1.88l-.06.06-8 8c-.21.2-.35.48-.41.77l-.5 2.5a1.5 1.5 0 001.76 1.76l2.5-.5c.3-.06.56-.2.77-.4L21 15.11V19a2 2 0 01-2 2H5a2 2 0 01-2-2V5zm18 4v4l-5 5-2.5.5.5-2.5 7-7zm-9-6H6v9l3-3 3 3V3z\" fill=\"currentColor\"/></svg>"
    }
}

fun RBuilder.logo(classes: String? = null) {
    child(logo) {
        attrs.classes = classes
    }
}
