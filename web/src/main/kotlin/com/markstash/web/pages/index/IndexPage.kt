package com.markstash.web.pages.index

import com.markstash.api.models.Resource
import react.RProps
import react.child
import react.dom.*
import react.functionalComponent

interface IndexPageProps : RProps {
    var resourceType: Resource.Type?
}

val indexPage = functionalComponent<IndexPageProps> { props ->
    div("flex flex-grow overflow-hidden") {
        child(resourceList) {
            attrs.resourceType = props.resourceType
        }
        div("flex flex-col w-0 flex-1") {
            props.children()
        }
    }
}
