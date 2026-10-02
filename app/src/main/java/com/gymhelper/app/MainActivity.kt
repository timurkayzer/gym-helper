package com.gymhelper.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gymhelper.app.ui.GymHelperNavHost
import com.gymhelper.app.ui.theme.GymHelperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GymHelperTheme {
                GymHelperNavHost()
            }
        }
    }
}
