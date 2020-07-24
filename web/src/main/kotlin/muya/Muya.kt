@file:JsModule("marktext/src/muya/lib")
@file:JsNonModule

package muya

import org.w3c.dom.Element

external interface MuyaOptions {
    var markdown: String
}

@JsName("default")
external class Muya(container: Element, options: MuyaOptions = definedExternally) {
    companion object {
        fun use(plugin: dynamic, options: dynamic = definedExternally)
    }

    fun on(event: String, handler: dynamic)
    fun destroy()
}

external interface CursorPosition {
    val line: Int
    val ch: Int
}

external interface TocEntry {
    val content: String
    val lvl: Int
    val slug: String
}

external interface WordCount {
    val all: Int
    val word: Int
    val paragraph: Int
    val character: Int
}

external interface ChangeEvent {
    val cursor: CursorPosition
    val markdown: String
    val toc: Array<TocEntry>
    val wordCount: WordCount
}
