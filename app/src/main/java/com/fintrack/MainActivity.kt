package com.fintrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.fintrack.app.feature.security.presentation.AppLockGate
import com.fintrack.core.designsystem.theme.FinTrackTheme
import com.fintrack.navigation.FinTrackNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinTrackTheme {
                AppLockGate(modifier = Modifier.fillMaxSize()) {
                    FinTrackNavHost(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}
