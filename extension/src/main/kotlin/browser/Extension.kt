package browser

import org.w3c.dom.Window

external interface Extension {
    val getBackgroundPage: (() -> Window)?
}
