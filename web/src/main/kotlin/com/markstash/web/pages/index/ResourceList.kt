package com.markstash.web.pages.index

import com.markstash.api.models.Bookmark
import com.markstash.api.models.Note
import com.markstash.api.models.Resource
import com.markstash.shared.js.api.bookmarksApi
import com.markstash.shared.js.api.notesApi
import com.markstash.shared.js.api.resourcesApi
import com.markstash.shared.js.components.resourceTag
import com.markstash.shared.js.helpers.rawHtml
import com.markstash.web.useStore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLInputElement
import react.RBuilder
import react.RProps
import react.child
import react.dom.*
import react.functionalComponent
import react.key
import react.router.dom.navLink
import react.useEffect
import react.useEffectWithCleanup
import react.useState
import com.markstash.api.bookmarks.SearchRequest as BookmarksSearchRequest
import com.markstash.api.notes.SearchRequest as NotesSearchRequest
import com.markstash.api.resources.SearchRequest as ResourcesSearchRequest

private interface ResourceRowProps : RProps {
    var listResourceType: Resource.Type?
    var resource: Resource
    var onTagClick: ((String) -> Unit)?
    var showFullExcerpt: Boolean
}

private val resourceRow = functionalComponent<ResourceRowProps> { props ->
    val link = "${if (props.listResourceType == null) "/everything" else ""}/${props.resource.type.name.toLowerCase()}s/${props.resource.id}"
    navLink<RProps>(to = link, className = "resource-row block px-4 py-4 border-b border-gray-200", activeClassName = "bg-indigo-50") {
        div("flex items-center") {
            if (props.listResourceType == null) {
                div("flex-no-shrink mr-4") {
                    rawHtml("w-6 h-6 text-gray-700") {
                        when (props.resource.type) {
                            Resource.Type.BOOKMARK -> "<svg fill=\"currentColor\" viewBox=\"0 0 20 20\"><path d=\"M5 4a2 2 0 012-2h6a2 2 0 012 2v14l-5-2.5L5 18V4z\"></path></svg>"
                            Resource.Type.NOTE -> "<svg fill=\"currentColor\" viewBox=\"0 0 20 20\"><path d=\"M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z\"></path></svg>"
                        }
                    }
                }
            }
            div("w-0 flex-grow") {
                rawHtml("text-sm leading-5 font-medium text-gray-900 truncate") {
                    props.resource.title.takeUnless { it.isNullOrBlank() } ?: "Untitled"
                }
                rawHtml("text-sm text-gray-500 ${if (props.showFullExcerpt) "" else "truncate"}") {
                    props.resource.excerpt.takeUnless { it.isNullOrBlank() } ?: "No description"
                }
                div("overflow-hidden") {
                    if (props.resource.tags.isEmpty()) {
                        span("text-sm text-gray-500") { +"No tags" }
                    } else {
                        props.resource.tags.forEach { tag ->
                            child(resourceTag) {
                                attrs.tag = tag
                                attrs.onClick = { e ->
                                    props.onTagClick?.let { callback ->
                                        e.preventDefault()
                                        callback(tag)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

interface ResourceListProps : RProps {
    var resourceType: Resource.Type?
}

private val searchInput = Channel<String>()

val resourceList = functionalComponent<ResourceListProps> { props ->
    val (isLoading, setIsLoading) = useState(true)
    val resources = useStore(ResourceStore, ResourceStoreState::resources)
    val (error, setError) = useState<String?>(null)
    val (search, setSearch) = useState("")

    suspend fun loadResources() {
        try {
            setIsLoading(true)
            setError(null)
            val newResources = when (props.resourceType) {
                Resource.Type.BOOKMARK -> bookmarksApi.index().map(Bookmark::toResource)
                Resource.Type.NOTE -> notesApi.index().map(Note::toResource)
                else -> resourcesApi.index()
            }
            ResourceStore.setResources(newResources)
            setIsLoading(false)
        } catch (e: Throwable) {
            setError(e.message ?: "Error loading bookmarks")
            setIsLoading(false)
        }
    }

    useEffectWithCleanup(listOf()) {
        val job = GlobalScope.launch {
            searchInput.receiveAsFlow().debounce(150).collect { input ->
                try {
                    if (input.isBlank()) return@collect loadResources()

                    setError(null)
                    val newResources = when (props.resourceType) {
                        Resource.Type.BOOKMARK ->
                            bookmarksApi.search(BookmarksSearchRequest(input)).results.map(Bookmark::toResource)
                        Resource.Type.NOTE ->
                            notesApi.search(NotesSearchRequest(input)).results.map(Note::toResource)
                        else -> resourcesApi.search(ResourcesSearchRequest(input)).results
                    }
                    ResourceStore.setResources(newResources)
                    setError(null)
                } catch (e: Throwable) {
                    setError(e.message ?: "Error loading bookmarks")
                }
            }
        }

        return@useEffectWithCleanup { job.cancel() }
    }

    useEffect(listOf()) {
        ResourceStore.clearResources()
        GlobalScope.launch { loadResources() }
    }

    fun RBuilder.renderSearchField() {
        div("relative z-10 flex-shrink-0 flex h-16 bg-white shadow") {
            button(classes = "px-4 border-r border-gray-200 text-gray-500 focus:outline-none focus:bg-gray-100 focus:text-gray-600 md:hidden") {
                rawHtml("h-6 w-6") {
                    "<svg fill=\"currentColor\" viewBox=\"0 0 20 20\"><path fill-rule=\"evenodd\" d=\"M3 5a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zM3 10a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zM3 15a1 1 0 011-1h6a1 1 0 110 2H4a1 1 0 01-1-1z\" clip-rule=\"evenodd\"></path></svg>"
                }
            }
            div("flex-1 px-4 flex justify-between") {
                div("flex-1 flex") {
                    form(classes = "w-full flex md:ml-0") {
                        label("sr-only") {
                            attrs.htmlFor = "search"
                        }
                        div("relative w-full text-gray-400 focus-within:text-gray-600") {
                            div("absolute inset-y-0 left-0 flex items-center pointer-events-none") {
                                rawHtml("h-5 w-5") {
                                    "<svg fill=\"currentColor\" viewBox=\"0 0 20 20\"><path fill-rule=\"evenodd\" d=\"M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z\" clip-rule=\"evenodd\"></path></svg>"
                                }
                            }
                            input(type = InputType.search, classes = "block w-full h-full pl-8 pr-3 py-2 rounded-md text-gray-900 placeholder-gray-500 focus:outline-none focus:placeholder-gray-400 sm:text-sm") {
                                attrs.value = search
                                attrs.placeholder = "Search"
                                attrs.onChangeFunction = {
                                    val value = (it.currentTarget as HTMLInputElement).value
                                    setSearch(value)
                                    GlobalScope.launch {
                                        searchInput.send(value)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    div("flex flex-col flex-shrink-0 w-96 overflow-hidden border-r") {
        renderSearchField()
        when {
            isLoading -> {
                p { +"Loading..." }
            }
            error != null -> {
                p { +"Error: $error" }
            }
            resources.isEmpty() -> {
                p { +"No bookmarks found" }
            }
            else -> {
                div("flex-1 overflow-y-auto bg-white") {
                    resources.forEach { resource ->
                        child(resourceRow) {
                            attrs.key = "${resource.type}-${resource.id}"
                            attrs.listResourceType = props.resourceType
                            attrs.resource = resource
                            attrs.showFullExcerpt = search.isNotBlank()
                            attrs.onTagClick = { tag ->
                                setSearch(tag)
                                GlobalScope.launch {
                                    searchInput.send(tag)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
