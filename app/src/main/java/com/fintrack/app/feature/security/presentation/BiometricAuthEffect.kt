package com.fintrack.app.feature.security.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
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
) {
    val context = LocalContext.current
    val authenticator = remember(context.applicationContext) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            SecurityEntryPoint::class.java,
        ).biometricPromptAuthenticator()
    }
    val activity = context as? FragmentActivity

    LaunchedEffect(requestAuth, activity) {
        if (!requestAuth || activity == null) return@LaunchedEffect
        authenticator.authenticate(activity, subtitleResId) { result ->
            onResult(result)
            onRequestConsumed()
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
