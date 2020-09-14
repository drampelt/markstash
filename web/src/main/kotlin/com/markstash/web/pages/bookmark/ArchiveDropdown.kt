package com.markstash.web.pages.bookmark

import com.markstash.api.archives.FetchRequest
import com.markstash.api.models.Archive
import com.markstash.api.models.Bookmark
import com.markstash.shared.js.api.archivesApi
import com.markstash.shared.js.helpers.rawHtml
import com.markstash.shared.js.icons.icon
import com.markstash.web.components.Dropdown
import com.markstash.web.components.modal
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.ButtonType
import kotlinx.html.js.onClickFunction
import react.RProps
import react.child
import react.dom.*
import react.functionalComponent
import react.useState

private interface FetchArchivesModalProps : RProps {
    var isOpen: Boolean
    var onRequestClose: () -> Unit
    var bookmark: Bookmark
}

private val FetchArchivesModal = functionalComponent<FetchArchivesModalProps> { props ->
    val useMemo = js("require('react').useMemo")
    val visibleTypes = useMemo({
        return@useMemo Archive.Type.values().filter { it.previewType != Archive.Type.PreviewType.NONE }
    }, emptyArray<Any>()).unsafeCast<List<Archive.Type>>()

    val (isSaving, setIsSaving) = useState(false)
    val (error, setError) = useState<String?>(null)
    val (selectedArchives, setSelectedArchives) = useState(visibleTypes)

    fun handleFetch() = GlobalScope.launch {
        setIsSaving(true)
        try {
            archivesApi.fetch(props.bookmark.id, FetchRequest(selectedArchives))
            setIsSaving(false)
            props.onRequestClose()
        } catch (e: Throwable) {
            setIsSaving(false)
            setError(e.message ?: "Error fetching archives")
        }
    }

    fun toggleArchive(type: Archive.Type) {
        if (type in selectedArchives) {
            setSelectedArchives(selectedArchives - type)
        } else {
            setSelectedArchives(selectedArchives + type)
        }
    }

    child(modal) {
        attrs.contentLabel = "Fetch new archives"
        attrs.isOpen = props.isOpen
        attrs.onRequestClose = { if (!isSaving) props.onRequestClose() }

        div("sm:flex sm:items-start") {
            div("mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left") {
                h3("text-lg leading-6 font-medium text-gray-900") { +"Fetch Archives" }
                div("mt-2") {
                    p("text-sm leading-5 text-gray-500") {
                        +"Choose which archives you would like to fetch."
                    }
                }
                div("flex flex-wrap -mx-2 mt-4") {
                    visibleTypes.forEach { type ->
                        div("w-1/2 p-2") {
                            div("flex items-center border ${if (type in selectedArchives) "border-indigo-500 shadow-sm" else "border-gray-300"} transition duration-150 rounded p-2 cursor-pointer") {
                                attrs.onClickFunction = { toggleArchive(type) }

                                rawHtml("w-4 h-4 mr-2 ${if (type in selectedArchives) "text-gray-400" else "text-gray-300"} transition duration-150") {
                                    type.icon()
                                }

                                div("text-sm flex-grow ${if (type in selectedArchives) "text-gray-700" else "text-gray-400"} transition duration-150") {
                                    +type.displayName
                                }

                                rawHtml("w-4 h-4 ml-2 text-indigo-500 transition duration-150 transform ${if (type in selectedArchives) "opacity-100 scale-100" else "opacity-0 scale-75"}") {
                                    "<svg xmlns=\"http://www.w3.org/2000/svg\" fill=\"none\" viewBox=\"0 0 24 24\" stroke=\"currentColor\"><path stroke-linecap=\"round\" stroke-linejoin=\"round\" stroke-width=\"2\" d=\"M5 13l4 4L19 7\" /></svg>"
                                }
                            }
                        }
                    }
                }
                if (error != null) {
                    div("mt-2") {
                        p("text-sm leading-5 text-red-600") { +error }
                    }
                }
            }
        }
        div("mt-5 sm:mt-4 sm:flex sm:flex-row-reverse") {
            span("flex w-full rounded-md shadow-sm sm:ml-3 sm:w-auto") {
                button(type = ButtonType.button, classes = "inline-flex justify-center w-full rounded-md border border-transparent px-4 py-2 bg-indigo-600 text-base leading-6 font-medium text-white shadow-sm hover:bg-indigo-500 focus:outline-none focus:border-indigo-700 focus:shadow-outline-indigo transition ease-in-out duration-150 sm:text-sm sm:leading-5") {
                    attrs.disabled = isSaving
                    attrs.onClickFunction = { handleFetch() }
                    +(if (isSaving) "Starting fetch..." else "Fetch")
                }
            }
            span("mt-3 flex w-full rounded-md shadow-sm sm:mt-0 sm:w-auto") {
                button(type = ButtonType.button, classes = "inline-flex justify-center w-full rounded-md border border-gray-300 px-4 py-2 bg-white text-base leading-6 font-medium text-gray-700 shadow-sm hover:text-gray-500 focus:outline-none focus:border-blue-300 focus:shadow-outline-blue transition ease-in-out duration-150 sm:text-sm sm:leading-5") {
                    attrs.disabled = isSaving
                    attrs.onClickFunction = { props.onRequestClose() }
                    +"Cancel"
                }
            }
        }
    }
}

private interface ArchiveTypeRowProps : RProps {
    var type: Archive.Type
    var archives: List<Archive>
    var onSelectArchive: (Archive) -> Unit
}

private val ArchiveTypeRow = functionalComponent<ArchiveTypeRowProps> { props ->
    val href = if (props.type.previewType == Archive.Type.PreviewType.EXTERNAL || props.type.previewType == Archive.Type.PreviewType.DOWNLOAD) {
        val archive = props.archives.first()
        "/api/bookmarks/${archive.bookmarkId}/archives/${archive.id}"
    } else null
    a(href = href, target = "_blank", classes = "group flex items-center px-4 py-2 cursor-pointer text-sm leading-5 text-gray-700 hover:bg-gray-100 hover:text-gray-900 focus:outline-none focus:bg-gray-100 focus:text-gray-900") {
        if (href == null) attrs.onClickFunction = { props.onSelectArchive(props.archives.first()) }
        rawHtml("mr-3 h-5 w-5 text-gray-400 group-hover:text-gray-500 group-focus:text-gray-500") {
            props.type.icon()
        }
        +(props.type.displayName)
    }
}

interface ArchiveDropdownProps : RProps {
    var isOpen: Boolean
    var bookmark: Bookmark
    var onSelectArchive: (Archive) -> Unit
    var onClickOut: (() -> Unit)?
}

val ArchiveDropdown = functionalComponent<ArchiveDropdownProps> { props ->
    val (isFetchModalOpen, setIsFetchModalOpen) = useState(false)
    val useMemo = js("require('react').useMemo")
    val groupedArchives = useMemo({
        return@useMemo (props.bookmark.archives ?: emptyList())
            .filter { it.type != Archive.Type.PLAIN && it.type != Archive.Type.FAVICON }
            .groupBy(Archive::type)
            .entries
            .groupBy { (type, _) -> type.previewType }
            .filter { (previewType, _) -> previewType != Archive.Type.PreviewType.NONE }
    }, arrayOf(props.bookmark)).unsafeCast<Map<Archive.Type.PreviewType, List<Map.Entry<Archive.Type, List<Archive>>>>>()

    child(Dropdown) {
        attrs.isOpen = props.isOpen
        attrs.onClickOut = props.onClickOut

        if (groupedArchives.isEmpty()) {
            div("py-1") {
                div("group flex items-center px-4 py-2 text-sm leading-5 text-gray-500") {
                    +"No archives"
                }
            }

            div("border-t border-gray-100") {}
        }

        groupedArchives.entries
            .sortedBy { (previewType, _) -> previewType.ordinal }
            .forEach { (_, groupedArchives) ->
                div("py-1") {
                    groupedArchives
                        .sortedBy { (type, _) -> type.ordinal }
                        .forEach { (type, archives) ->
                            child(ArchiveTypeRow) {
                                attrs.type = type
                                attrs.archives = archives
                                attrs.onSelectArchive = props.onSelectArchive
                            }
                        }
                }
                div("border-t border-gray-100") {}
            }

        div("border-t border-gray-100") {}

        div("py-1") {
            a(href = null, classes = "group flex items-center px-4 py-2 cursor-pointer text-sm leading-5 text-gray-700 hover:bg-gray-100 hover:text-gray-900 focus:outline-none focus:bg-gray-100 focus:text-gray-900") {
                rawHtml("mr-3 h-5 w-5 text-gray-400 group-hover:text-gray-500 group-focus:text-gray-500") {
                    "<svg xmlns=\"http://www.w3.org/2000/svg\" fill=\"none\" viewBox=\"0 0 24 24\" stroke=\"currentColor\"><path stroke-linecap=\"round\" stroke-linejoin=\"round\" stroke-width=\"2\" d=\"M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M9 19l3 3m0 0l3-3m-3 3V10\" /></svg>"
                }
                attrs.onClickFunction = {
                    props.onClickOut?.invoke()
                    setIsFetchModalOpen(true)
                }
                +"Fetch new archives..."
            }
            a(href = null, classes = "group flex items-center px-4 py-2 cursor-pointer text-sm leading-5 text-gray-700 hover:bg-gray-100 hover:text-gray-900 focus:outline-none focus:bg-gray-100 focus:text-gray-900") {
                rawHtml("mr-3 h-5 w-5 text-gray-400 group-hover:text-gray-500 group-focus:text-gray-500") {
                    "<svg xmlns=\"http://www.w3.org/2000/svg\" fill=\"none\" viewBox=\"0 0 24 24\" stroke=\"currentColor\"><path stroke-linecap=\"round\" stroke-linejoin=\"round\" stroke-width=\"2\" d=\"M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4\" /></svg>"
                }
                +"Manage archives..."
            }
        }
    }

    child(FetchArchivesModal) {
        attrs.isOpen = isFetchModalOpen
        attrs.onRequestClose = { setIsFetchModalOpen(false) }
        attrs.bookmark = props.bookmark
    }
}
