//
//  LoginScreen.swift
//  Markstash
//
//  Created by Daniel Rampelt on 2020-12-29.
//

import SwiftUI
import shared

struct LoginForm: View {
    let callback: (_ email: String, _ password: String) -> Void

    @State private var email = ""
    @State private var password = ""

    var body: some View {
        VStack {
            TextField("Email", text: $email)
                .keyboardType(.emailAddress)
                .textContentType(.emailAddress)
                .autocapitalization(.none)
                .padding()
                .overlay(
                    RoundedRectangle(cornerRadius: 8)
                        .stroke(Color.blue, lineWidth: 1)
                )
            SecureField("Password", text: $password)
                .textContentType(.password)
                .padding()
                .overlay(
                    RoundedRectangle(cornerRadius: 8)
                        .stroke(Color.blue, lineWidth: 1)
                )
            Spacer()
                .frame(height: 24)
            Button(action: { callback(email, password) }) {
                Text("Login")
                    .padding()
                    .frame(maxWidth: .infinity)
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(8)
            }
        }
        .padding()
    }
}

struct LoginScreen: View {
    @ObservedViewModel private var viewModel = LoginViewModel(session: Session.instance, sessionsApi: Apis.instance.sessions)

    @State private var email = ""
    @State private var password = ""
    @State private var isSettingsOpen = false

    var body: some View {
        let errorBinding = Binding<Bool>(
            get: { $viewModel.error != nil },
            set: { if !$0 { viewModel.dismissError() } }
        )

        NavigationView {
            VStack {
                Text("Markstash")
                    .font(.title)
                Spacer()
                    .frame(height: 48)
                LoginForm(callback: { viewModel.login(email: $0, password: $1) })
            }
            .alert(isPresented: errorBinding) {
                Alert(
                    title: Text("Error logging in"),
                    message: Text($viewModel.error?.description() ?? "An unknown error occurred"),
                    dismissButton: .default(Text("Okay"))
                )
            }
            .navigationBarItems(
                leading: Button(action: { isSettingsOpen = true }) {
                    Image(systemName: "gear")
                }
            )
            .sheet(
                isPresented: $isSettingsOpen,
                onDismiss: {
                    // Need to use the updated API client after changing the base URL
                    _viewModel.replace(with: LoginViewModel(session: Session.instance, sessionsApi: Apis.instance.sessions))
                }
            ) {
                LoginSettingsScreen()
            }
        }
        .onDisappear { viewModel.clear() }
    }
}

struct LoginScreen_Previews: PreviewProvider {
    static var previews: some View {
        LoginForm(callback: { _, _ in })
    }
}
