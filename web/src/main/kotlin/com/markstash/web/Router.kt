package com.markstash.web

import com.markstash.api.models.Resource
import com.markstash.web.layout.authenticatedLayout
import com.markstash.web.pages.bookmark.BookmarkPageProps
import com.markstash.web.pages.bookmark.bookmarkPage
import com.markstash.web.pages.index.indexPage
import com.markstash.web.pages.loginPage
import com.markstash.web.pages.note.NotePageProps
import com.markstash.web.pages.note.notePage
import react.RProps
import react.child
import react.dom.*
import react.functionalComponent
import react.router.dom.browserRouter
import react.router.dom.redirect
import react.router.dom.route
import react.router.dom.switch

val routes = functionalComponent<RProps> {
    browserRouter {
        switch {
            route("/login", strict = true) {
                child(loginPage)
            }
            route<RProps>("/") { layoutProps ->
                child(authenticatedLayout) {
                    attrs.history = layoutProps.history
                    route("/", exact = true) {
                        redirect(to = "/everything")
                    }
                    route("/everything") {
                        child(indexPage) {
                            route<BookmarkPageProps>("/everything/bookmarks/:id") { props ->
                                child(bookmarkPage) {
                                    attrs.id = props.match.params.id
                                }
                            }
                            route<NotePageProps>("/everything/notes/:id") { props ->
                                child(notePage) {
                                    attrs.id = props.match.params.id
                                }
                            }
                        }
                    }
                    route("/bookmarks") {
                        child(indexPage) {
                            attrs.resourceType = Resource.Type.BOOKMARK

                            route<BookmarkPageProps>("/bookmarks/:id") { props ->
                                child(bookmarkPage) {
                                    attrs.id = props.match.params.id
                                }
                            }
                        }
                    }
                    route("/notes") {
                        child(indexPage) {
                            attrs.resourceType = Resource.Type.NOTE

                            route<NotePageProps>("/notes/:id") { props ->
                                child(notePage) {
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

