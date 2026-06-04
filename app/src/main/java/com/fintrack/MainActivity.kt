package com.fintrack

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.fintrack.app.feature.security.presentation.AppLockGate
import com.fintrack.app.feature.security.presentation.AppLockViewModel
import com.fintrack.core.designsystem.theme.FinTrackTheme
import com.fintrack.navigation.FinTrackNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val appLockViewModel: AppLockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinTrackTheme {
                AppLockGate(
                    viewModel = appLockViewModel,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    FinTrackNavHost(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appLockViewModel.onAppResumed()
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) {
            appLockViewModel.lockWhenBackgrounded()
        }
    }
}
