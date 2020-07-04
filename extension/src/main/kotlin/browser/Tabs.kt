package browser

import kotlin.js.Promise

external interface Tabs {
    fun query(queryInfo: QueryInfo): Promise<Array<Tab>>
}

external interface QueryInfo {
    var active: Boolean?
    var currentWindow: Boolean?
}

external interface Tab {
    val title: String?
    val url: String?
}

