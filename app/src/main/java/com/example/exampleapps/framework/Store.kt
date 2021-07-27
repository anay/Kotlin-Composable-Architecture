package com.example.exampleapps.framework

import arrow.optics.Optional
import kotlinx.coroutines.flow.*

typealias Effect<Action> = Flow<Action>
typealias Reducer<State, Action> = suspend (State, Action) -> Pair<State, Effect<Action>>

data class Store<State,  Action>(
    private val initialState: State,
    private val reducer: Reducer<State, Action>
){
    private val stateData = MutableStateFlow(initialState)
    val state = stateData.asStateFlow()

    suspend fun send(action:Action){
        val currentState = stateData.value
        val (nextState, effect) = reducer(currentState, action)
        stateData.value = nextState
        effect.collect(this::send)
    }

    fun <ViewState, ViewAction> forView(
        appState: State,
        stateBuilder: (State) -> ViewState,
        actionMapper: (ViewAction) -> Action
    ): Store<ViewState, ViewAction> = Store(
        initialState = stateBuilder(appState),
        reducer = { _, viewAction ->
            send(actionMapper(viewAction))
            Pair(
                stateBuilder(this.state.value),
                emptyFlow()
            )
        }
    )

}

fun <State, Action, ViewState, ViewAction> pullBack(
    reducer: Reducer<ViewState, ViewAction>,
    stateMapper: Optional<State, ViewState>,
    actionMapper: Optional<Action, ViewAction>
): Reducer<State, Action> = { state, action ->
    val viewState = stateMapper.getOrNull(state)
    val viewAction = actionMapper.getOrNull(action)
    if (viewState != null && viewAction != null){
        val (nextViewState, nextEffects) = reducer(viewState, viewAction)
        Pair(
            stateMapper.set(state, nextViewState),
            nextEffects.map { actionMapper.set(action, it) }
        )
    } else {
        Pair(
            state,
            emptyFlow()
        )
    }
}

fun <State, Action> combine(
    vararg reducers: Reducer<State, Action>
): Reducer<State, Action> = { state, action ->
    reducers.fold(Pair(state, emptyFlow())){ result, reducer ->
        var (nextState, nextEffects) = reducer(result.first, action)
        Pair(
            nextState,
            flowOf(result.second, nextEffects).flattenMerge()
        )
    }
}