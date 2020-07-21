package com.markstash.web.pages

import com.markstash.api.sessions.LoginResponse
import com.markstash.shared.js.components.loginForm
import com.markstash.web.Session
import react.RProps
import react.child
import react.dom.*
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
        div("min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8") {
            div("sm:mx-auto sm:w-full sm:max-w-md") {
                h1("text-center text-5xl leading-9 text-gray-900") {
                    +"Markstash"
                }
                h2("text-center text-3xl leading-9 font-extrabold text-gray-900 mt-8") {
                    +"Sign in"
                }
            }
            div("mt-8 sm:mx-auto sm:w-full sm:max-w-md") {
                div("bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10") {
                    child(loginForm) {
                        attrs.onLogIn = { handleLogIn(it) }
                    }
                }
            }
        }
    }
}
