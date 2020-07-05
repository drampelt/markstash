package browser

import kotlin.js.Promise

external interface Storage {
    val local: StorageArea
    val sync: StorageArea?
    val managed: StorageArea?
}

external interface StorageArea {
    fun get(): Promise<dynamic>
    fun get(key: String): Promise<dynamic>
    fun get(vararg keys: String): Promise<dynamic>
    fun set(keys: dynamic): Promise<Unit>
    fun remove(key: String): Promise<Unit>
    fun remove(keys: Array<String>): Promise<Unit>
    fun clear(): Promise<Unit>
}
