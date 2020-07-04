package browser.tabs

inline fun QueryInfo(block: QueryInfo.() -> Unit) = (js("{}").unsafeCast<QueryInfo>()).apply(block)
