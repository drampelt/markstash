package com.markstash.mobile

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class StateFlowObserver<T>(val stateFlow: StateFlow<T>) {
    val job = SupervisorJob()
    val scope = CoroutineScope(Dispatchers.Main + job)

    fun observe(observer: (T) -> Unit) {
        stateFlow.onEach { observer(it) }
            .launchIn(scope)
    }

    fun unsubscribe() {
        job.cancelChildren()
    }
}
