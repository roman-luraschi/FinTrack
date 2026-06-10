package com.fintrack.core.domain.model

enum class MercadoPagoConnectionStatus {
    DISCONNECTED,
    CONNECTED,
    ERROR,
}

data class MercadoPagoConnectionState(
    val status: MercadoPagoConnectionStatus = MercadoPagoConnectionStatus.DISCONNECTED,
    val errorMessage: String? = null,
)

data class MercadoPagoOAuthCallback(
    val success: Boolean,
    val deviceId: String?,
    val reason: String?,
)
