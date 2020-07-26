package com.markstash.web

import com.markstash.shared.js.api.apiClient
import muya.Muya
import muya.plugins.CodePicker
import muya.plugins.EmojiPicker
import muya.plugins.FootnoteTool
import muya.plugins.FormatPicker
import muya.plugins.FrontMenu
import muya.plugins.ImagePicker
import muya.plugins.ImageSelector
import muya.plugins.ImageToolbar
import muya.plugins.LinkTools
import muya.plugins.QuickInsert
import muya.plugins.TableTools
import muya.plugins.TablePicker
import muya.plugins.Transformer
import react.child
import react.dom.*
import react.modal.ReactModal
import kotlin.browser.document
import kotlin.browser.window

fun main() {
    js("require('css/main.css');")

    initMuya()

    apiClient.baseUrl = "${window.location.protocol}//${window.location.hostname}:${window.location.port}/api"

    ReactModal.setAppElement("#app")
    render(document.getElementById("app")) {
        child(routes)
    }
}

private fun initMuya() {
    Muya.use(TablePicker)
    Muya.use(QuickInsert)
    Muya.use(CodePicker)
    Muya.use(EmojiPicker)
    Muya.use(ImagePicker)
    Muya.use(ImageSelector)
    Muya.use(Transformer)
    Muya.use(ImageToolbar)
    Muya.use(FormatPicker)
    Muya.use(FrontMenu)
    Muya.use(LinkTools)
    Muya.use(FootnoteTool)
    Muya.use(TableTools)
}
