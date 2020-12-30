package com.markstash.mobile

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

expect open class BaseViewModel() {
    val scope: CoroutineScope

    protected open fun onCleared()
}

open class StateViewModel<T>(initialState: T) : BaseViewModel() {
    protected val _state = MutableStateFlow(initialState)
    val state: StateFlow<T> = _state

    val currentState: T
        get() = state.value

    protected fun setState(newState: T) {
        _state.value = newState
    }

    fun observeState(observer: ((T) -> Unit)) {
        state.onEach { observer(it) }
            .launchIn(scope)
    }
}
