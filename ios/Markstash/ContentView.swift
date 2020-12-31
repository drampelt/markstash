//
//  ContentView.swift
//  Markstash
//
//  Created by Daniel Rampelt on 2020-09-23.
//

import SwiftUI
import shared

struct ContentView: View {
    var body: some View {
        Button(action: { Session.instance.logout() }) {
            Text("Log Out")
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
