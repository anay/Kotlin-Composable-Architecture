package com.example.exampleapps.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import arrow.optics.optics
import com.example.exampleapps.framework.Reducer
import com.example.exampleapps.framework.Store
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

@optics
data class CounterState(val counter:Int, val navigateTo:String){
    companion object
}

@optics
sealed class CounterActions{
    companion object
    object DelayedIncrement: CounterActions()
    object Increment: CounterActions()
    object Decrement: CounterActions()
    object NextScreen: CounterActions()
}


val counterReducer: Reducer<CounterState, CounterActions, Unit> = { state, action, _->
    when(action){
        CounterActions.NextScreen -> Pair(
            state.copy(navigateTo = "selectedData"),
            emptyFlow()
        )
        CounterActions.DelayedIncrement -> Pair(
            state,
            flow<CounterActions> {
                delay(10000)
                emit(CounterActions.Increment)
            }
        )
        CounterActions.Increment -> Pair(
            state.copy(counter = state.counter + 1),
            emptyFlow()
        )
        CounterActions.Decrement -> Pair(
            state.copy(counter = state.counter - 1),
            emptyFlow()
        )
    }
}

@Composable
fun Counter(store: Store<CounterState, CounterActions>) {
    val state by store.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    MaterialTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Button(onClick = {
                    coroutineScope.launch {
                        store.send(CounterActions.Decrement)
                    }
                }) {
                    Text("-")
                }
                Text("${state.counter}", textAlign = TextAlign.Center)
                Button(onClick = {
                    coroutineScope.launch {
                        store.send(CounterActions.Increment)
                    }
                }) {
                    Text("+")
                }
            }
            Button(onClick = { coroutineScope.launch { store.send(CounterActions.NextScreen) } }) {
                Text("Next")
            }
        }
    }
}