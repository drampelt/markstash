package com.markstash.extension.popup

import com.markstash.shared.js.helpers.rawHtml
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLInputElement
import react.RProps
import react.dom.*
import react.functionalComponent
import react.useState

interface SettingsPageProps : RProps {
    var config: Configuration
    var onUpdateBaseUrl: (String) -> Unit
    var onBackClicked: () -> Unit
}

val settingsPage = functionalComponent<SettingsPageProps> { props ->
    val (baseUrl, setBaseUrl) = useState(props.config.baseUrl ?: "")

    fun handleBackClicked() {
        props.onUpdateBaseUrl(baseUrl)
        props.onBackClicked()
    }

    div("p-4 w-full max-w-md") {
        a(classes = "cursor-pointer") {
            attrs.onClickFunction = { handleBackClicked() }
            rawHtml("w-5 h-5") {
                "<svg viewBox=\"0 0 20 20\" fill=\"currentColor\"><path fill-rule=\"evenodd\" d=\"M9.707 16.707a1 1 0 01-1.414 0l-6-6a1 1 0 010-1.414l6-6a1 1 0 011.414 1.414L5.414 9H17a1 1 0 110 2H5.414l4.293 4.293a1 1 0 010 1.414z\" clip-rule=\"evenodd\"></path></svg>"
            }
        }
        div("mt-4") {
            label("block text-sm font-medium leading-5 text-gray-700") {
                attrs.htmlFor = "address"
                +"Server Address"
            }
            div("mt-1 relative rounded-md shadow-sm") {
                input(type = InputType.url, classes = "form-input block w-full sm:text-sm sm:leading-5") {
                    attrs.placeholder = "https://app.markstash.com/api"
                    attrs.id = "address"
                    attrs.value = baseUrl
                    attrs.onChangeFunction = { e ->
                        setBaseUrl((e.currentTarget as HTMLInputElement).value)
                    }
                }
            }
            p("mt-2 text-sm text-gray-500") {
                +"Enter the address of your self-hosted server, or leave it blank to use markstash.com. In most cases, you should include /api at the end."
            }
        }
    }
}
