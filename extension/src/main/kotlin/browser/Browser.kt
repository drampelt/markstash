package browser

@JsModule("webextension-polyfill")
@JsNonModule
external object browser {
    val tabs: Tabs
    val extension: Extension
    val storage: Storage
}
