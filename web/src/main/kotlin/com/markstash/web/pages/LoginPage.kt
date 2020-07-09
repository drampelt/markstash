package com.markstash.web.pages

import com.markstash.api.sessions.LoginResponse
import com.markstash.shared.js.components.loginForm
import react.RProps
import react.child
import react.functionalComponent
import react.router.dom.RouteResultProps

interface LoginPageProps : RouteResultProps<RProps>

val loginPage = functionalComponent<LoginPageProps> { props ->
    fun handleLogIn(response: LoginResponse) {
        console.log("login success", response)
    }

    child(loginForm) {
        attrs.onLogIn = { handleLogIn(it) }
    }
}
