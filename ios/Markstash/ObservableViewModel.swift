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
    let viewModel: VM
    @Published var state: S

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
}

@propertyWrapper struct ObservedViewModel<S, VM : StateViewModel<S>>: DynamicProperty {
    @StateObject var viewModel: ObservableViewModel<S, VM>

    init(_ viewModel: VM) {
        _viewModel = StateObject(wrappedValue: ObservableViewModel(viewModel: viewModel))
    }

    init(wrappedValue: VM) {
        _viewModel = StateObject(wrappedValue: ObservableViewModel(viewModel: wrappedValue))
    }

    public var wrappedValue: VM { viewModel.viewModel }

    public var projectedValue: S { viewModel.state }
}
