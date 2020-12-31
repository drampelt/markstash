//
//  MarkstashApp.swift
//  Markstash
//
//  Created by Daniel Rampelt on 2020-09-23.
//

import SwiftUI
import shared

@main
struct MarkstashApp: App {
    @ObservedStateFlow<KotlinBoolean>(Session.instance.isLoggedInFlow) private var isLoggedIn

    var body: some Scene {
        WindowGroup {
            if isLoggedIn.boolValue {
                ContentView()
            } else {
                LoginScreen()
            }
        }
    }
}
