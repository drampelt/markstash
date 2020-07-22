package com.markstash.web.layout

import com.markstash.shared.js.api.apiClient
import com.markstash.shared.js.helpers.rawHtml
import com.markstash.web.Session
import react.RBuilder
import react.RProps
import react.child
import react.dom.*
import react.functionalComponent
import react.router.dom.RouteResultProps
import react.router.dom.navLink
import react.router.dom.redirect
import react.useEffect
import react.useState

private interface SidebarLinkProps : RProps {
    var label: String
    var path: String
    var icon: String
    var exact: Boolean?
}

private val sidebarLink = functionalComponent<SidebarLinkProps> { props ->
    // group flex items-center px-2 py-2 text-sm leading-5 font-medium text-white rounded-md bg-gray-900 focus:outline-none focus:bg-gray-700 transition ease-in-out duration-150
    navLink<RProps>(
        to = props.path,
        className = "mt-1 group flex items-center p-2 text-sm leading-5 font-medium text-gray-300 rounded-md hover:text-white hover:bg-gray-700 focus:outline-none focus:text-white focus:bg-gray-700 transition ease-in-out duration-150",
        activeClassName = "text-white bg-gray-900",
        exact = props.exact ?: false
    ) {
        rawHtml("mr-3 h06 w-6 text-gray-400 group-hover:text-gray-300 group-focus:text-gray-300 transition ease-in-out duration-150") {
            props.icon
        }
        +props.label
    }
}

interface AuthenticatedLayoutProps : RouteResultProps<RProps>

val authenticatedLayout = functionalComponent<AuthenticatedLayoutProps> { props ->
    val (shouldLogin, setShouldLogin) = useState(false)
    val (didAuthenticate, setDidAuthenticate) = useState(Session.isAuthenticated)

    fun handleInitialLogin() {
        Session.loginWithExistingToken { success ->
            setShouldLogin(!success)
            setDidAuthenticate(success)
        }
    }

    useEffect(listOf()) {
        if (!Session.isAuthenticated && Session.isInitialLoad && apiClient.authToken != null) {
            handleInitialLogin()
        } else {
            setShouldLogin(true)
        }
    }

    fun RBuilder.renderSidebar() {
        div("hidden md:flex md:flex-shrink-0") {
            div("flex flex-col w-64 bg-gray-800") {
                div("flex items-center justify-center h-16 flex-shrink-0 px-4") {
                    div("text-white text-2xl bold") { +"Markstash" }
                }
                div("flex-1 flex flex-col overflow-y-auto") {
                    nav("flex-1 px-2 py-4") {
                        child(sidebarLink) {
                            attrs.label = "Everything"
                            attrs.path = "/"
                            attrs.icon = "<svg fill=\"currentColor\" viewBox=\"0 0 20 20\"><path d=\"M10.707 2.293a1 1 0 00-1.414 0l-7 7a1 1 0 001.414 1.414L4 10.414V17a1 1 0 001 1h2a1 1 0 001-1v-2a1 1 0 011-1h2a1 1 0 011 1v2a1 1 0 001 1h2a1 1 0 001-1v-6.586l.293.293a1 1 0 001.414-1.414l-7-7z\"></path></svg>"
                            attrs.exact = true
                        }
                        child(sidebarLink) {
                            attrs.label = "Bookmarks"
                            attrs.path = "/bookmarks"
                            attrs.icon = "<svg fill=\"currentColor\" viewBox=\"0 0 20 20\"><path d=\"M5 4a2 2 0 012-2h6a2 2 0 012 2v14l-5-2.5L5 18V4z\"></path></svg>"
                        }
                    }
                }
            }
        }
    }

    fun RBuilder.renderLayout() {
        div("h-screen flex overflow-hidden bg-gray-100") {
            renderSidebar()
            div("flex flex-col w-0 flex-1 overflow-hidden") {
                props.children()
            }
        }
    }

    when {
        didAuthenticate -> {
            renderLayout()
        }
        shouldLogin -> {
            redirect(to = "/login")
        }
        else -> {
            p { +"Loading..." }
        }
    }
}
