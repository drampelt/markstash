package com.markstash.mobile.ui.main

import com.markstash.api.models.Resource
import com.markstash.client.api.ResourcesApi
import com.markstash.mobile.StateViewModel
import kotlinx.coroutines.launch

data class ResourceListViewState(
    val isLoading: Boolean = false,
    val resources: List<Resource> = emptyList(),
    val error: Throwable? = null,
)

class ResourceListViewModel(
    private val resourcesApi: ResourcesApi,
) : StateViewModel<ResourceListViewState>(ResourceListViewState()) {

    init {
        loadResources()
    }

    fun loadResources() {
        viewModelScope.launch {
            setState(currentState.copy(isLoading = true))

            val resources = runCatching { resourcesApi.index() }
                .getOrElse {
                    setState(currentState.copy(isLoading = false, error = it))
                    return@launch
                }

            setState(currentState.copy(isLoading = false, resources = resources))
        }
    }
}
