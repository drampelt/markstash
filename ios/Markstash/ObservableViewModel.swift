//
//  ObservableViewModel.swift
//  Markstash
//
//  Created by Daniel Rampelt on 2020-12-27.
//

import Foundation
import SwiftUI
import shared

class ObservableViewModel<S, VM : StateViewModel<S>>: ObservableObject {
    @Published private(set) var viewModel: VM
    @Published private(set) var state: S
    @Published var id = UUID()

    init(viewModel: VM) {
        self.viewModel = viewModel
        state = viewModel.currentState!
        viewModel.observeState { newState in
            self.state = newState!
        }
    }

    func clear() {
        viewModel.clear()
    }

    func replace(with newViewModel: VM) {
        viewModel.clear()
        viewModel = newViewModel
        state = viewModel.currentState!
        viewModel.observeState { newState in
            self.state = newState!
        }
    }
}

@propertyWrapper struct ObservedViewModel<S, VM : StateViewModel<S>>: DynamicProperty {
    @StateObject var observer: ObservableViewModel<S, VM>

    init(_ viewModel: VM) {
        _observer = StateObject(wrappedValue: ObservableViewModel(viewModel: viewModel))
    }

    init(wrappedValue: VM) {
        _observer = StateObject(wrappedValue: ObservableViewModel(viewModel: wrappedValue))
    }

    public var wrappedValue: VM { observer.viewModel }

    public var projectedValue: S { observer.state }

    func clear() {
        observer.clear()
    }

    func replace(with newViewModel: VM) {
        observer.replace(with: newViewModel)
    }
}
