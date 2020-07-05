package com.markstash.extension

fun <T> dyn(block: T.() -> Unit): T = js("{}").unsafeCast<T>().apply(block)
