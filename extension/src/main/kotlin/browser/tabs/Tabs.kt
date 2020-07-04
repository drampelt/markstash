@file:JsQualifier("browser.tabs")

package browser.tabs

import kotlin.js.Promise

external fun query(queryInfo: QueryInfo): Promise<Array<Tab>>

external interface QueryInfo {
    var active: Boolean?
    var currentWindow: Boolean?
}

external interface Tab {
    val title: String?
    val url: String?
}

