package com.markstash.mobile

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren

actual open class BaseViewModel {
    private val viewModelJob = SupervisorJob()
    actual val viewModelScope: CoroutineScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    protected actual open fun onCleared() {
        viewModelJob.cancelChildren()
    }

    fun clear() {
        onCleared()
    }
}
