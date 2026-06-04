package com.fintrack.app.data.security

import android.content.Context
import androidx.biometric.BiometricManager
import com.fintrack.core.domain.model.BiometricAvailability
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricCapabilityChecker @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val authenticators =
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL

    fun check(): BiometricAvailability {
        return when (BiometricManager.from(context).canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.Ready
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NotEnrolled
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
            -> BiometricAvailability.NoHardware
            else -> BiometricAvailability.Unavailable
        }
    }

    val allowedAuthenticators: Int get() = authenticators
}
