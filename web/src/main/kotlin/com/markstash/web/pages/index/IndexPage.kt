package com.markstash.web.pages.index

import react.RProps
import react.child
import react.dom.*
import react.functionalComponent

val indexPage = functionalComponent<RProps> {
    div {
        child(bookmarkList)
    }
}
