package com.fintrack.app.feature.security.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.R
import com.fintrack.app.data.security.BiometricAuthResult

/**
 * Gates app access Mercado Pago-style: a dedicated unlock screen first, then the main app.
 */
@Composable
fun AppLockGate(
    viewModel: AppLockViewModel,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val showLockScreen = uiState.isLockStateReady &&
        uiState.lockEnabled &&
        !uiState.isUnlocked
    val mountMainApp = uiState.isLockStateReady &&
        (!uiState.lockEnabled || uiState.hasUnlockedOnce)

    if (showLockScreen) {
        BiometricAuthEffect(
            requestAuth = uiState.requestAuth,
            subtitleResId = R.string.biometric_lock_prompt,
            onResult = viewModel::onAuthResult,
            onRequestConsumed = viewModel::onAuthRequestConsumed,
            onAuthenticationStarted = viewModel::onAuthenticationStarted,
            onAuthenticationFinished = viewModel::onAuthenticationFinished,
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            !uiState.isLockStateReady -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            mountMainApp -> {
                content()
                if (showLockScreen) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        BiometricLockScreen(
                            isPromptRequested = uiState.requestAuth,
                            authError = uiState.authError,
                            onScreenDisplayed = viewModel::onLockScreenDisplayed,
                            onRetryClick = viewModel::retryAuth,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
            showLockScreen -> {
                BiometricLockScreen(
                    isPromptRequested = uiState.requestAuth,
                    authError = uiState.authError,
                    onScreenDisplayed = viewModel::onLockScreenDisplayed,
                    onRetryClick = viewModel::retryAuth,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
