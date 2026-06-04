package com.fintrack.app.data.security

import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.fintrack.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricPromptAuthenticator @Inject constructor(
    private val capabilityChecker: BiometricCapabilityChecker,
) {
    fun authenticate(
        activity: FragmentActivity,
        subtitleResId: Int = R.string.biometric_lock_subtitle,
        onResult: (BiometricAuthResult) -> Unit,
    ) {
        if (capabilityChecker.check() != com.fintrack.core.domain.model.BiometricAvailability.Ready) {
            onResult(BiometricAuthResult.Error(BiometricAuthError.Unavailable))
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onResult(BiometricAuthResult.Success)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onResult(BiometricAuthResult.Error(mapErrorCode(errorCode)))
            }

            override fun onAuthenticationFailed() {
                // Transient failure (e.g. wrong finger); prompt stays open.
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(R.string.biometric_lock_title))
            .setSubtitle(activity.getString(subtitleResId))
            .setAllowedAuthenticators(capabilityChecker.allowedAuthenticators)
            .build()

        prompt.authenticate(promptInfo)
    }

    private fun mapErrorCode(errorCode: Int): BiometricAuthError = when (errorCode) {
        BiometricPrompt.ERROR_USER_CANCELED,
        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
        -> BiometricAuthError.Canceled
        BiometricPrompt.ERROR_LOCKOUT,
        BiometricPrompt.ERROR_LOCKOUT_PERMANENT,
        -> BiometricAuthError.Lockout
        BiometricPrompt.ERROR_NO_BIOMETRIC,
        BiometricPrompt.ERROR_HW_UNAVAILABLE,
        BiometricPrompt.ERROR_SECURITY_UPDATE_REQUIRED,
        -> BiometricAuthError.Unavailable
        BiometricPrompt.ERROR_TIMEOUT -> BiometricAuthError.Timeout
        else -> BiometricAuthError.Unknown
    }
}
