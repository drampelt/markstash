//
//  Session.swift
//  Markstash
//
//  Created by Daniel Rampelt on 2020-12-30.
//

import Foundation
import shared

class Session {
    static var instance = shared.Session(listener: Listener())

    private class Listener: SessionListener {
        func onAuthTokenChange(authToken: String?) {
            Apis.instance = Apis(baseUrl: Apis.instance.client.baseUrl, authToken: authToken)
        }

        func onBaseUrlChange(baseUrl: String) {
            Apis.instance = Apis(baseUrl: baseUrl, authToken: nil)
        }
    }
}
