package com.markstash.web.pages.bookmark

import react.RProps
import react.dom.*
import react.functionalComponent

interface BookmarkPageProps : RProps {
    var id: Long
}

val bookmarkPage = functionalComponent<BookmarkPageProps> { props ->
    div { +"Bookmark id ${props.id}" }
}
