package com.fintrack.core.domain.model

sealed class BiometricAvailability {
    data object Ready : BiometricAvailability()
    data object NotEnrolled : BiometricAvailability()
    data object NoHardware : BiometricAvailability()
    data object Unavailable : BiometricAvailability()
}
