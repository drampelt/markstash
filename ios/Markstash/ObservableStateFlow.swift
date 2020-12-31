//
//  ObservableStateFlow.swift
//  Markstash
//
//  Created by Daniel Rampelt on 2020-12-30.
//

import Foundation
import SwiftUI
import shared

class ObservableStateFlow<T: AnyObject>: ObservableObject {
    @Published private(set) var value: T
    private let observer: StateFlowObserver<T>

    init(stateFlow: Kotlinx_coroutines_coreStateFlow) {
        observer = StateFlowObserver(stateFlow: stateFlow)
        value = stateFlow.value as! T
        observer.observe { newValue in
            self.value = newValue!
        }
    }

    func unsubscribe() {
        observer.unsubscribe()
    }
}

@propertyWrapper struct ObservedStateFlow<T: AnyObject>: DynamicProperty {
    @StateObject var observable: ObservableStateFlow<T>

    init(_ stateFlow: Kotlinx_coroutines_coreStateFlow) {
        _observable = StateObject(wrappedValue: ObservableStateFlow(stateFlow: stateFlow))
    }

    public var wrappedValue: T { observable.value }

    public var projectedValue: ObservableStateFlow<T> { observable }
}
