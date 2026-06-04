package com.fintrack.app.feature.security.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.R
import com.fintrack.app.data.security.BiometricAuthResult

@Composable
fun AppLockGate(
    modifier: Modifier = Modifier,
    viewModel: AppLockViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onColdStart()
    }

    BiometricAuthEffect(
        requestAuth = uiState.requestAuth,
        subtitleResId = R.string.biometric_lock_subtitle,
        onResult = viewModel::onAuthResult,
        onRequestConsumed = viewModel::onAuthRequestConsumed,
    )

    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.isUnlocked) {
            content()
        }
        if (uiState.showLockOverlay) {
            BiometricLockScreen(
                authError = uiState.authError,
                onUnlockClick = viewModel::retryAuth,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
