package com.markstash.web.pages.bookmark

import com.markstash.api.models.Archive
import com.markstash.api.models.Bookmark
import com.markstash.shared.js.helpers.rawHtml
import com.markstash.web.components.Dropdown
import kotlinx.html.js.onClickFunction
import react.RProps
import react.child
import react.dom.*
import react.functionalComponent

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
            when {
                props.type.previewType == Archive.Type.PreviewType.DOWNLOAD -> "<svg xmlns=\"http://www.w3.org/2000/svg\" fill=\"none\" viewBox=\"0 0 24 24\" stroke=\"currentColor\"><path stroke-linecap=\"round\" stroke-linejoin=\"round\" stroke-width=\"2\" d=\"M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4\" /></svg>"
                props.type.previewType == Archive.Type.PreviewType.EXTERNAL -> "<svg xmlns=\"http://www.w3.org/2000/svg\" fill=\"none\" viewBox=\"0 0 24 24\" stroke=\"currentColor\"><path stroke-linecap=\"round\" stroke-linejoin=\"round\" stroke-width=\"2\" d=\"M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14\" /></svg>"
                props.type == Archive.Type.SCREENSHOT || props.type == Archive.Type.SCREENSHOT_FULL -> "<svg xmlns=\"http://www.w3.org/2000/svg\" fill=\"none\" viewBox=\"0 0 24 24\" stroke=\"currentColor\"><path stroke-linecap=\"round\" stroke-linejoin=\"round\" stroke-width=\"2\" d=\"M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z\" /></svg>"
                props.type == Archive.Type.READABILITY || props.type == Archive.Type.MONOLITH_READABILITY -> "<svg xmlns=\"http://www.w3.org/2000/svg\" fill=\"none\" viewBox=\"0 0 24 24\" stroke=\"currentColor\"><path stroke-linecap=\"round\" stroke-linejoin=\"round\" stroke-width=\"2\" d=\"M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253\" /></svg>"
                else -> "<svg xmlns=\"http://www.w3.org/2000/svg\" fill=\"none\" viewBox=\"0 0 24 24\" stroke=\"currentColor\"><path stroke-linecap=\"round\" stroke-linejoin=\"round\" stroke-width=\"2\" d=\"M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z\" /></svg>"
            }
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
            .forEach { (type, groupedArchives) ->
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
}
