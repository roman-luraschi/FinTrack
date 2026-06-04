package com.fintrack.app.data.security

sealed class BiometricAuthResult {
    data object Success : BiometricAuthResult()
    data class Error(val type: BiometricAuthError) : BiometricAuthResult()
}

enum class BiometricAuthError {
    Canceled,
    Lockout,
    Unavailable,
    Timeout,
    Unknown,
}
