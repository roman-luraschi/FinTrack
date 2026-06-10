package com.fintrack.core.domain.repository

import com.fintrack.core.domain.common.DomainResult

interface FinTrackBackendPort {
    suspend fun pingHealth(): DomainResult<Boolean>
    suspend fun registerDevice(
        deviceId: String,
        appVersion: String? = null,
        fcmToken: String? = null,
    ): DomainResult<Unit>

    suspend fun startMercadoPagoOAuth(deviceId: String): DomainResult<String>
}
