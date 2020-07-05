package com.markstash.extension.popup

import com.markstash.api.sessions.LoginRequest
import com.markstash.api.sessions.LoginResponse
import com.markstash.extension.sessionsApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
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

    div {
        input(type = InputType.email) {
            attrs.placeholder = "Email"
            attrs.value = email
            attrs.onChangeFunction = { setEmail((it.currentTarget as HTMLInputElement).value) }
        }
        input(type = InputType.password) {
            attrs.placeholder = "Password"
            attrs.value = password
            attrs.onChangeFunction = { setPassword((it.currentTarget as HTMLInputElement).value) }
        }
        br {}
        button {
            +(if (isLoading) "Logging in..." else "Log In")
            attrs.disabled = isLoading
            attrs.onClickFunction = { handleLogIn() }
        }
        if (error != null) {
            p { +error }
        }
    }
}
