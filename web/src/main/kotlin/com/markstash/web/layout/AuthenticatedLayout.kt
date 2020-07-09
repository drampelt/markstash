package com.markstash.web.layout

import com.markstash.shared.js.api.apiClient
import com.markstash.web.Session
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

    when {
        didAuthenticate -> {
            props.children()
        }
        shouldLogin -> {
            redirect(to = "/login")
        }
        else -> {
            p { +"Loading..." }
        }
    }
}
