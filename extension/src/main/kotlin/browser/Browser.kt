package browser

@JsModule("webextension-polyfill")
@JsNonModule
external object browser {
    val tabs: Tabs
}
