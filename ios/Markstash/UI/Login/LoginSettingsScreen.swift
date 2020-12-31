//
//  LoginSettingsScreen.swift
//  Markstash
//
//  Created by Daniel Rampelt on 2020-12-30.
//

import SwiftUI

struct LoginSettingsScreen: View {
    @Environment(\.presentationMode) var presentationMode

    @State var serverAddress = Session.instance.baseUrl

    var body: some View {
        NavigationView {
            List {
                Section(header: Text("Server Address"), footer: Text("Enter the address of your self-hosted server, or leave it blank to use markstash.com. In most cases, you should include /api at the end.")) {
                    TextField(
                        "Server Address",
                        text: $serverAddress,
                        onEditingChanged: { if !$0 { Session.instance.baseUrl = serverAddress } },
                        onCommit: { Session.instance.baseUrl = serverAddress }
                    )
                }
            }
            .listStyle(InsetGroupedListStyle())
            .navigationTitle(Text("Settings"))
            .navigationBarItems(
                trailing: Button(action: { self.presentationMode.wrappedValue.dismiss() }) {
                    Text("Done")
                }
            )
        }
    }
}

struct LoginSettingsScreen_Previews: PreviewProvider {
    static var previews: some View {
        LoginSettingsScreen()
    }
}
