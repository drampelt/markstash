package com.markstash.web

import com.markstash.web.pages.note.NoteStore
import react.RDependenciesList
import react.RMutableRef
import react.useEffect
import react.useEffectWithCleanup
import react.useState

typealias StoreListener<T> = (T) -> Unit

abstract class Store<T>(initialState: T) {
    var state: T = initialState
        set(value) {
            field = value
            notifyListeners()
        }

    protected var listeners = mutableListOf<StoreListener<T>>()

    fun listen(listener: StoreListener<T>) {
        listeners.add(listener)
    }

    fun unlisten(listener: StoreListener<T>) {
        listeners.remove(listener)
    }

    protected fun notifyListeners() {
        listeners.forEach { it.invoke(state) }
    }
}

fun <T, R> useStore(store: Store<T>, getter: (T) -> R) = useStore(store, listOf(), getter)

fun <T, R> useStore(store: Store<T>, dependencies: RDependenciesList, getter: (T) -> R): R {
    val currentState = getter(store.state)
    val (state, setState) = useState(currentState)
    val stateRef = js("require('react').useRef()").unsafeCast<RMutableRef<R>>()
    stateRef.current = state

    val listenerRef = js("require('react').useRef()").unsafeCast<RMutableRef<StoreListener<T>>>()

    useEffect(listOf(currentState)) {
        if (stateRef.current != currentState) {
            setState(currentState)
        }
    }

    useEffectWithCleanup(dependencies) {
        listenerRef.current = { storeState ->
            val nextState = getter(storeState)
            if (stateRef.current != nextState) {
                setState(nextState)
            }
        }

        store.listen(listenerRef.current)

        return@useEffectWithCleanup {
            store.unlisten(listenerRef.current)
        }
    }

    return state
}
