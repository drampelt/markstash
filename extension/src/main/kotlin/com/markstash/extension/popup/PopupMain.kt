package com.markstash.extension.popup

import browser.browser
import com.markstash.api.models.User
import com.markstash.api.sessions.LoginResponse
import com.markstash.shared.js.api.apiClient
import com.markstash.shared.js.components.loginForm
import com.markstash.shared.js.helpers.rawHtml
import kotlinext.js.Object
import kotlinext.js.jsObject
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
import kotlinx.browser.document

fun popupMain() {
    js("require('css/main.css');")
    render(document.getElementById("main")) {
        child(popup)
    }
}

interface Configuration {
    var authToken: String?
    var user: User?
    var baseUrl: String?
}

val popup = functionalComponent<RProps> {
    val (isLoading, setIsLoading) = useState(true)
    val (config, setConfig) = useState<Configuration>(jsObject())
    val (showSettings, setShowSettings) = useState(false)

    useEffect(listOf()) {
        GlobalScope.launch {
            val storedConfig = browser.storage.local.get().asDeferred().await().unsafeCast<Configuration>()
            apiClient.authToken = storedConfig.authToken
            apiClient.baseUrl = storedConfig.baseUrl?.takeIf { it.isNotBlank() } ?: "https://app.markstash.com/api"
            setConfig(storedConfig)
            setIsLoading(false)
        }
    }

    fun handleLogIn(response: LoginResponse) = GlobalScope.launch {
        val newConfig = jsObject<Configuration> {
            authToken = response.authToken
            user = response.user
            baseUrl = config.baseUrl
        }
        browser.storage.local.set(newConfig).asDeferred().await()
        apiClient.authToken = response.authToken
        setConfig(newConfig)
    }

    fun handleLogOut() = GlobalScope.launch {
        browser.storage.local.clear().asDeferred().await()
        val newConfig = jsObject<Configuration> {
            baseUrl = config.baseUrl
        }
        setConfig(newConfig)
    }

    fun handleUpdateBaseUrl(baseUrl: String) = GlobalScope.launch {
        val newConfig = Object.assign(jsObject(), config, jsObject { this.baseUrl = baseUrl })
        browser.storage.local.set(newConfig).asDeferred().await()
        apiClient.baseUrl = baseUrl.takeIf { it.isNotBlank() } ?: "https://app.markstash.com/api"
        setConfig(newConfig)
    }

    val user = config.user
    when {
        isLoading -> {
            p { +"Loading..." }
        }
        user == null -> {
            if (showSettings) {
                child(settingsPage) {
                    attrs.config = config
                    attrs.onBackClicked = { setShowSettings(false) }
                    attrs.onUpdateBaseUrl = { handleUpdateBaseUrl(it) }
                }
            } else {
                div("w-full max-w-md px-4 py-4") {
                    a(classes = "cursor-pointer") {
                        attrs.onClickFunction = { setShowSettings(true) }
                        rawHtml("w-4 h-4") {
                            "<svg viewBox=\"0 0 20 20\" fill=\"currentColor\"><path fill-rule=\"evenodd\" d=\"M11.49 3.17c-.38-1.56-2.6-1.56-2.98 0a1.532 1.532 0 01-2.286.948c-1.372-.836-2.942.734-2.106 2.106.54.886.061 2.042-.947 2.287-1.561.379-1.561 2.6 0 2.978a1.532 1.532 0 01.947 2.287c-.836 1.372.734 2.942 2.106 2.106a1.532 1.532 0 012.287.947c.379 1.561 2.6 1.561 2.978 0a1.533 1.533 0 012.287-.947c1.372.836 2.942-.734 2.106-2.106a1.533 1.533 0 01.947-2.287c1.561-.379 1.561-2.6 0-2.978a1.532 1.532 0 01-.947-2.287c.836-1.372-.734-2.942-2.106-2.106a1.532 1.532 0 01-2.287-.947zM10 13a3 3 0 100-6 3 3 0 000 6z\" clip-rule=\"evenodd\"></path></svg>"
                        }
                    }
                    h1("mt-4 text-center text-xl font-extrabold text-gray-900") {
                        +"Sign in to Markstash"
                    }
                    div("mt-8") {
                        child(loginForm) {
                            attrs.onLogIn = { handleLogIn(it) }
                        }
                    }
                }
            }
        }
        else -> {
            child(bookmarkForm) {
                attrs.onLogOut = { handleLogOut() }
            }
        }
    }
}
