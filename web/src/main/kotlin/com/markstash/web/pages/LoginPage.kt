package com.markstash.web.pages

import com.markstash.api.sessions.LoginResponse
import com.markstash.shared.js.components.loginForm
import com.markstash.web.Session
import react.RProps
import react.child
import react.functionalComponent
import react.router.dom.redirect
import react.useState

val loginPage = functionalComponent<RProps> {
    val (isAuthenticated, setIsAuthenticated) = useState(Session.isAuthenticated)

    fun handleLogIn(response: LoginResponse) {
        Session.login(response)
        setIsAuthenticated(true)
    }

    if (isAuthenticated) {
        redirect(to = "/")
    } else {
        child(loginForm) {
            attrs.onLogIn = { handleLogIn(it) }
        }
    }
}
