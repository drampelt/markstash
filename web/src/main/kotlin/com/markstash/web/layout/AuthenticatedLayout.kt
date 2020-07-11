package com.markstash.web.layout

import com.markstash.shared.js.api.apiClient
import com.markstash.web.Session
import react.RBuilder
import react.RProps
import react.dom.*
import react.functionalComponent
import react.router.dom.RouteResultProps
import react.router.dom.redirect
import react.useEffect
import react.useState

interface AuthenticatedLayoutProps : RouteResultProps<RProps>

val authenticatedLayout = functionalComponent<AuthenticatedLayoutProps> { props ->
    val (shouldLogin, setShouldLogin) = useState(false)
    val (didAuthenticate, setDidAuthenticate) = useState(Session.isAuthenticated)

    fun handleInitialLogin() {
        Session.loginWithExistingToken { success ->
            console.log("login", success)
            setShouldLogin(!success)
            setDidAuthenticate(success)
        }
    }

    useEffect(listOf()) {
        if (!Session.isAuthenticated && Session.isInitialLoad && apiClient.authToken != null) {
            handleInitialLogin()
        }
    }

    fun RBuilder.renderSidebar() {
        div("hidden md:flex md:flex-shrink-0 border-r") {
            div("flex flex-col w-64") {
                +"sidebar"
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
