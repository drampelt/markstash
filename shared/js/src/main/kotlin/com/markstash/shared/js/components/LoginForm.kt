package com.markstash.shared.js.components

import com.markstash.api.sessions.LoginRequest
import com.markstash.api.sessions.LoginResponse
import com.markstash.shared.js.api.sessionsApi
import com.markstash.shared.js.helpers.rawHtml
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.ButtonType
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.HTMLInputElement
import react.RProps
import react.dom.*
import react.functionalComponent
import react.useState

interface LoginProps : RProps {
    var onLogIn: (LoginResponse) -> Unit
}

val loginForm = functionalComponent<LoginProps> { props ->
    val (email, setEmail) = useState("")
    val (password, setPassword) = useState("")
    val (isLoading, setIsLoading) = useState(false)
    val (error, setError) = useState<String?>(null)

    fun handleLogIn() = GlobalScope.launch {
        setIsLoading(true)
        val result = try {
            sessionsApi.login(LoginRequest(email, password))
        } catch (e: Exception) {
            setError(e.message ?: "Error logging in")
            setIsLoading(false)
            return@launch
        }
        setIsLoading(false)
        props.onLogIn(result)
    }

    form {
        attrs.onSubmitFunction = { e ->
            e.preventDefault()
            handleLogIn()
        }

        div {
            label("block text-sm font-medium leading-5 text-gray-700") {
                attrs.htmlFor = "email"
                +"Email address"
            }
            div("mt-1 rounded-md shadow-sm") {
                input(type = InputType.email, classes = "appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md placeholder-gray-400 focus:outline-none focus:shadow-outline-blue focus:border-blue-300 transition duration-150 ease-in-out sm:text-sm sm:leading-5") {
                    attrs.id = "email"
                    attrs.placeholder = "Email"
                    attrs.value = email
                    attrs.onChangeFunction = { setEmail((it.currentTarget as HTMLInputElement).value) }
                }
            }
        }
        div("mt-6") {
            label("block text-sm font-medium leading-5 text-gray-700") {
                attrs.htmlFor = "password"
                +"Password"
            }
            div("mt-1 rounded-md shadow-sm") {
                input(type = InputType.password, classes = "appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md placeholder-gray-400 focus:outline-none focus:shadow-outline-blue focus:border-blue-300 transition duration-150 ease-in-out sm:text-sm sm:leading-5") {
                    attrs.id = "password"
                    attrs.placeholder = "Password"
                    attrs.value = password
                    attrs.onChangeFunction = { setPassword((it.currentTarget as HTMLInputElement).value) }
                }
            }
        }
        div("mt-6") {
            span("block w-full rounded-md shadow-sm") {
                button(classes = "w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-500 focus:outline-none focus:border-indigo-700 focus:shadow-outline-indigo active:bg-indigo-700 transition duration-150 ease-in-out", type = ButtonType.submit) {
                    +(if (isLoading) "Logging in..." else "Log In")
                    attrs.disabled = isLoading
                    attrs.onClickFunction = { handleLogIn() }
                }
            }
        }
        if (error != null) {
            div("flex items-center text-red-600 mt-4") {
                rawHtml("w-5 h-5") {
                    "<svg fill=\"currentColor\" viewBox=\"0 0 20 20\"><path fill-rule=\"evenodd\" d=\"M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z\" clip-rule=\"evenodd\"></path></svg>"
                }
                div("ml-2") {
                    +error
                }
            }
        }
    }
}
