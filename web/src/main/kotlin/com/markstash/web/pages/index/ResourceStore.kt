package com.markstash.web.pages.index

import com.markstash.api.models.Resource
import react.RMutableRef
import react.useEffectWithCleanup
import react.useState

data class ResourceStoreState(
    val resources: List<Resource> = emptyList()
)

typealias ResourceStoreListener = (ResourceStoreState) -> Unit

object ResourceStore {
    var state = ResourceStoreState()
        private set(value) {
            field = value
            listeners.forEach { it.invoke(value) }
        }

    private var listeners = mutableListOf<ResourceStoreListener>()

    fun listen(listener: ResourceStoreListener) {
        listeners.add(listener)
    }

    fun unlisten(listener: ResourceStoreListener) {
        listeners.remove(listener)
    }

    fun clearResources() {
        state = state.copy(resources = emptyList())
    }

    fun setResources(resources: List<Resource>) {
        state = state.copy(resources = resources)
    }

    fun updateResource(newResource: Resource) {
        val newResources = state.resources.map { oldResource ->
            if (oldResource.type == newResource.type && oldResource.id == newResource.id) newResource else oldResource
        }
        state = state.copy(resources = newResources)
    }

    fun addResource(newResource: Resource) {
        state = state.copy(resources = listOf(newResource) + state.resources)
    }
}

fun <T> useResourceStore(getter: (ResourceStoreState) -> T): T {
    val (state, setState) = useState(getter(ResourceStore.state))
    val stateRef = js("require('react').useRef()").unsafeCast<RMutableRef<T>>()
    stateRef.current = state

    val listenerRef = js("require('react').useRef()").unsafeCast<RMutableRef<ResourceStoreListener>>()

    useEffectWithCleanup(listOf()) {
        listenerRef.current = { storeState ->
            val nextState = getter(storeState)
            if (stateRef.current != nextState) {
                setState(nextState)
            }
        }

        ResourceStore.listen(listenerRef.current)

        return@useEffectWithCleanup {
            ResourceStore.unlisten(listenerRef.current)
        }
    }

    return state
}
