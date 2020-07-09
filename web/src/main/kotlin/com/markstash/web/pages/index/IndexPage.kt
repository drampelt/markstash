package com.markstash.web.pages.index

import react.RProps
import react.child
import react.dom.*
import react.functionalComponent

val indexPage = functionalComponent<RProps> { props ->
    div {
        child(bookmarkList)
        hr {}
        props.children()
    }
}
