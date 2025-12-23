package com.example.loancalculatorwiamkotlin.redux

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log

class Store<State, Action>(
    initialState: State,
    private val reducer: (State, Action) -> State,
    private val middleware: List<suspend (Store<State, Action>, State, Action) -> Unit> = emptyList()
) {
    private val middlewareScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state

    fun dispatch(action: Action) {
        val currentState = _state.value
        val newState = reducer(currentState, action)
        val stateChanged = currentState != newState
        _state.value = newState

        middleware.forEach { mw ->
            middlewareScope.launch {
                mw(this@Store, newState, action)
            }
        }
    }
}
