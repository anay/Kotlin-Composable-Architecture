package com.example.exampleapps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.exampleapps.framework.Store

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val store = Store.of(
            state = AppState(0, "counter"),
            reducer = appReducer,
            environment = Unit
        )
        setContent {
            App(store)
        }
    }

}



