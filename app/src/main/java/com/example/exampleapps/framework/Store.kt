package com.example.exampleapps.framework

import arrow.optics.Lens
import arrow.optics.Optional
import kotlinx.coroutines.flow.*

typealias Effect<Action> = Flow<Action>
typealias Reducer<State, Action, AppEnvironment> = suspend (State, Action, AppEnvironment) -> Pair<State, Effect<Action>>

class Store<State,  Action> private constructor(
    private val initialState: State,
    private val reducer: suspend (State, Action) -> Pair<State, Effect<Action>>
){

    companion object{
        fun <State, Action, AppEnvironment> of(state: State, reducer:Reducer<State, Action, AppEnvironment>, environment: AppEnvironment) =
            Store<State, Action>(
                initialState = state,
                reducer = {viewState, viewAction -> reducer(viewState, viewAction, environment)}
            )
    }
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
    ): Store<ViewState, ViewAction> = of<ViewState, ViewAction, Unit>(
        state = stateBuilder(appState),
        reducer = { _, viewAction, _ ->
            send(actionMapper(viewAction))
            Pair(
                stateBuilder(this.state.value),
                emptyFlow()
            )
        },
        environment = Unit
    )

}

fun <State, Action, ViewState, ViewAction, AppEnvironment, ViewEnvironment> pullBack(
    reducer: Reducer<ViewState, ViewAction, ViewEnvironment>,
    stateMapper: Lens<State, ViewState>,
    actionMapper: Optional<Action, ViewAction>,
    environmentMapper: (AppEnvironment) -> ViewEnvironment
): Reducer<State, Action, AppEnvironment> = { state, action, environment ->
    val viewState:ViewState = stateMapper.get(state)
    val viewAction = actionMapper.getOrNull(action)
    if (viewAction != null){
        val (nextViewState, nextEffects) = reducer(viewState, viewAction, environmentMapper(environment))
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

fun <State, Action, AppEnvironment> combine(
    vararg reducers: Reducer<State, Action, AppEnvironment>
): Reducer<State, Action, AppEnvironment> = { state, action, environment ->
    reducers.fold(Pair(state, emptyFlow())){ result, reducer ->
        val (nextState, nextEffects) = reducer(result.first, action, environment)
        Pair(
            nextState,
            flowOf(result.second, nextEffects).flattenMerge()
        )
    }
}