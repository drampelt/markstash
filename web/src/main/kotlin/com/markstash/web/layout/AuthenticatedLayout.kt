package com.markstash.web.layout

import com.markstash.shared.js.api.apiClient
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
}

private val sidebarLink = functionalComponent<SidebarLinkProps> { props ->
    navLink<RProps>(to = props.path, className = "w-full p-2 mb-1 font-medium text-gray-600 rounded hover:text-gray-900 hover:bg-gray-200 focus:outline-none focus:bg-gray-300", activeClassName = "text-gray-900 bg-gray-200") {
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
        div("hidden md:flex md:flex-shrink-0 border-r") {
            div("flex flex-col w-64") {
                div("text-center text-2xl bold mt-4") { +"Markstash" }
                nav("flex flex-col w-full p-2 mt-4") {
                    child(sidebarLink) {
                        attrs.label = "Everything"
                        attrs.path = "/"
                    }
                    child(sidebarLink) {
                        attrs.label = "Bookmarks"
                        attrs.path = "/bookmarks"
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
