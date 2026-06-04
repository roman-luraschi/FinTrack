package com.fintrack.app.feature.security.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.withResumed
import com.fintrack.app.data.security.BiometricAuthResult
import com.fintrack.app.data.security.BiometricPromptAuthenticator
import com.fintrack.app.di.SecurityEntryPoint
import dagger.hilt.android.EntryPointAccessors

@Composable
fun BiometricAuthEffect(
    requestAuth: Boolean,
    subtitleResId: Int,
    onResult: (BiometricAuthResult) -> Unit,
    onRequestConsumed: () -> Unit,
    onAuthenticationStarted: () -> Unit = {},
    onAuthenticationFinished: () -> Unit = {},
) {
    val context = LocalContext.current
    val authenticator = remember(context.applicationContext) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            SecurityEntryPoint::class.java,
        ).biometricPromptAuthenticator()
    }
    val activity = context as? FragmentActivity
    val currentOnResult = rememberUpdatedState(onResult)
    val currentOnConsumed = rememberUpdatedState(onRequestConsumed)
    val currentOnAuthStart = rememberUpdatedState(onAuthenticationStarted)
    val currentOnAuthEnd = rememberUpdatedState(onAuthenticationFinished)

    LaunchedEffect(requestAuth, activity) {
        if (!requestAuth) return@LaunchedEffect
        val host = activity ?: return@LaunchedEffect
        host.lifecycle.withResumed {
            currentOnAuthStart.value()
            authenticator.authenticate(host, subtitleResId) { result ->
                currentOnAuthEnd.value()
                currentOnResult.value(result)
                currentOnConsumed.value()
            }
        }
    }
}

@Composable
fun rememberBiometricAuthenticator(): BiometricPromptAuthenticator {
    val context = LocalContext.current
    return remember(context.applicationContext) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            SecurityEntryPoint::class.java,
        ).biometricPromptAuthenticator()
    }
}
