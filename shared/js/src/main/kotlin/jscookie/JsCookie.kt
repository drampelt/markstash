package jscookie

@JsModule("js-cookie")
@JsNonModule
external object Cookies {
    fun get(cookie: String): String?
    fun set(name: String, value: String, options: CookieOptions? = definedExternally)
    fun remove(name: String, options: CookieOptions? = definedExternally)
}

external interface CookieOptions {
    var expires: Int?
    var path: String?
    var domain: String?
}
