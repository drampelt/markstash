package com.markstash.web

import com.markstash.web.layout.authenticatedLayout
import com.markstash.web.pages.bookmark.BookmarkPageProps
import com.markstash.web.pages.bookmark.bookmarkPage
import com.markstash.web.pages.index.indexPage
import com.markstash.web.pages.loginPage
import react.RProps
import react.child
import react.dom.*
import react.functionalComponent
import react.router.dom.browserRouter
import react.router.dom.route
import react.router.dom.switch

val routes = functionalComponent<RProps> {
    browserRouter {
        switch {
            route("/login", strict = true) {
                child(loginPage)
            }
            route("/") {
                child(authenticatedLayout) {
                    route("/") {
                        child(indexPage) {
                            route<BookmarkPageProps>("/bookmarks/:id") { props ->
                                child(bookmarkPage) {
                                    attrs.id = props.match.params.id
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

