package com.markstash.web.pages.index

import react.RProps
import react.child
import react.dom.*
import react.functionalComponent

val indexPage = functionalComponent<RProps> { props ->
    div("flex flex-grow overflow-hidden") {
        child(bookmarkList)
        div("flex flex-col w-0 flex-1") {
            props.children()
        }
    }
}
