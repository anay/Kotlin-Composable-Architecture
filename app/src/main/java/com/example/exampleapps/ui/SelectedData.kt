package com.example.exampleapps.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import arrow.optics.optics
import com.example.exampleapps.framework.Reducer
import com.example.exampleapps.framework.Store
import com.example.exampleapps.framework.StoreView
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

@optics
data class SelectedDataState(val count:Int, val navigateTo: String){
    companion object
}

@optics
sealed class SelectedDataActions{
    companion object
    object OkClicked: SelectedDataActions()
    object Reset: SelectedDataActions()
}

val selectedDataReducer: Reducer<SelectedDataState, SelectedDataActions, Unit> = { state, actions,_ ->
    when(actions){

        SelectedDataActions.OkClicked -> Pair(
            state.copy(navigateTo = "counter"),
            emptyFlow()
        )

        SelectedDataActions.Reset -> Pair(
            state.copy(count = 0),
            emptyFlow()
        )
    }
}

@Composable
fun SelectedData(store: Store<SelectedDataState, SelectedDataActions>){
    StoreView(store) { state ->
        MaterialTheme {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxHeight()
            ) {
                Text(
                    "Count:"
                )
                Text(state.count.toString())
                Button(onClick = sendToStore(SelectedDataActions.OkClicked)) {
                    Text("Ok")
                }
                Button(onClick = sendToStore(SelectedDataActions.Reset)) {
                    Text("Reset")
                }
            }
        }
    }

}