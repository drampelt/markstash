package com.markstash.web

import com.markstash.web.pages.loginPage
import react.RProps
import react.child
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
            redirect("/", "/login")
        }
    }
}

