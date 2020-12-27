package com.markstash.mobile

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope

actual open class BaseViewModel : ViewModel() {
    actual val viewModelScope: CoroutineScope
        get() = TODO("Not yet implemented")

    actual override fun onCleared() = super.onCleared()
}
