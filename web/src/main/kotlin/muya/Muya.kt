@file:JsModule("marktext/src/muya/lib")
@file:JsNonModule

package muya

import org.w3c.dom.Element

external interface MuyaOptions

@JsName("default")
external class Muya(container: Element, options: MuyaOptions = definedExternally) {
    companion object {
        fun use(plugin: dynamic, options: dynamic = definedExternally)
    }

    fun destroy()
}
