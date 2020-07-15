package com.markstash.extension.popup

import browser.browser
import com.markstash.api.models.User
import com.markstash.api.sessions.LoginResponse
import com.markstash.extension.dyn
import com.markstash.shared.js.api.apiClient
import com.markstash.shared.js.components.loginForm
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.launch
import kotlinx.html.js.onClickFunction
import react.RProps
import react.child
import react.dom.*
import react.functionalComponent
import react.useEffect
import react.useState
import kotlin.browser.document

fun popupMain() {
    js("require('css/main.css');")
    render(document.getElementById("main")) {
        child(popup)
    }
}

interface Configuration {
    var authToken: String?
    var user: User?
}

val popup = functionalComponent<RProps> {
    val (isLoading, setIsLoading) = useState(true)
    val (config, setConfig) = useState<Configuration>(dyn {  })

    useEffect(listOf()) {
        GlobalScope.launch {
            val storedConfig = browser.storage.local.get().asDeferred().await().unsafeCast<Configuration>()
            apiClient.authToken = storedConfig.authToken
            setConfig(storedConfig)
            setIsLoading(false)
        }
    }

    fun handleLogIn(response: LoginResponse) = GlobalScope.launch {
        val newConfig = dyn<Configuration> {
            authToken = response.authToken
            user = response.user
        }
        browser.storage.local.set(newConfig).asDeferred().await()
        apiClient.authToken = response.authToken
        setConfig(newConfig)
    }

    fun handleLogOut() = GlobalScope.launch {
        browser.storage.local.clear().asDeferred().await()
        val newConfig = dyn<Configuration> {}
        setConfig(newConfig)
    }

    val user = config.user
    when {
        isLoading -> {
            p { +"Loading..." }
        }
        user == null -> {
            child(loginForm) {
                attrs.onLogIn = { handleLogIn(it) }
            }
        }
        else -> {
            child(bookmarkForm)
            hr {}
            p { +user.email }
            button {
                +"Log Out"
                attrs.onClickFunction = { handleLogOut() }
            }
        }
    }
}
