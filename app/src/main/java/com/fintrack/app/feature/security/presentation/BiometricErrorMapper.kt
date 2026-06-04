package com.fintrack.app.feature.security.presentation

import androidx.annotation.StringRes
import com.fintrack.R
import com.fintrack.app.data.security.BiometricAuthError

@StringRes
fun BiometricAuthError.toMessageRes(): Int = when (this) {
    BiometricAuthError.Canceled -> R.string.biometric_error_canceled
    BiometricAuthError.Lockout -> R.string.biometric_error_lockout
    BiometricAuthError.Unavailable -> R.string.biometric_error_unavailable
    BiometricAuthError.Timeout,
    BiometricAuthError.Unknown,
    -> R.string.biometric_error_unavailable
}

@StringRes
fun com.fintrack.core.domain.model.BiometricAvailability.toHelpMessageRes(): Int? = when (this) {
    com.fintrack.core.domain.model.BiometricAvailability.Ready -> null
    com.fintrack.core.domain.model.BiometricAvailability.NotEnrolled -> R.string.biometric_not_enrolled
    com.fintrack.core.domain.model.BiometricAvailability.NoHardware -> R.string.biometric_no_hardware
    com.fintrack.core.domain.model.BiometricAvailability.Unavailable -> R.string.biometric_error_unavailable
}
