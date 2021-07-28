package com.example.exampleapps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import arrow.core.none
import arrow.core.toOption
import arrow.optics.*
import arrow.optics.Optional
import com.example.exampleapps.framework.Reducer
import com.example.exampleapps.framework.Store
import com.example.exampleapps.framework.pullBack
import com.example.exampleapps.ui.*

@Composable
fun App(store: Store<AppState, AppActions>) {
    val state by store.state.collectAsState()
    when (state.navigateTo) {
        "selectedData" -> SelectedData(
            store.forView(
                state,
                stateBuilder = { it.selectedDataState },
                actionMapper = { AppActions.SelectedData(it) }
            )
        )

        "counter" -> Counter(
            store.forView(
                state,
                stateBuilder = { it.counterState },
                actionMapper = { AppActions.Counter(it) }
            )
        )
    }
}

val appReducer: Reducer<AppState, AppActions, Unit> = com.example.exampleapps.framework.combine(
    pullBack(
        reducer = counterReducer,
        stateMapper = AppState.counterState,
        actionMapper = AppActions.Counter,
        environmentMapper = { }
    ),
    pullBack(
        reducer = selectedDataReducer,
        stateMapper = AppState.selectedDataState,
        actionMapper = AppActions.SelectedData,
        environmentMapper = { }
    )
)

@optics
data class AppState(val count:Int, val navigateTo:String="counter"){
    companion object{
        val counterState:Lens<AppState, CounterState> = Lens(
            get = { it.counterState },
            set = { appState, counterState ->
                appState.copy(
                    count = counterState.counter,
                    navigateTo = counterState.navigateTo
                )
            }
        )

        val selectedDataState:Lens<AppState, SelectedDataState> = Lens(
            get = { it.selectedDataState },
            set = { appState, selectedData ->
                appState.copy(
                    count = selectedData.count,
                    navigateTo = selectedData.navigateTo
                )
            }
        )
    }

    val counterState
        get() = CounterState(count, navigateTo)

    val selectedDataState
        get() = SelectedDataState(count, navigateTo)
}

sealed class AppActions{
    companion object{
        val Counter = Optional<AppActions, CounterActions>(
            getOption = { if (it is Counter) it.action.toOption() else none() },
            set = { _, action -> Counter(action) }
        )

        val SelectedData = Optional<AppActions, SelectedDataActions>(
            getOption = { if (it is SelectedData) it.action.toOption() else none() },
            set = { _, action -> SelectedData(action) }
        )
    }
    data class Counter(val action: CounterActions): AppActions(){
        companion object
    }
    data class SelectedData(val action: SelectedDataActions): AppActions(){
        companion object
    }
}