package com.fintrack.core.domain.usecase.mercadopago

import com.fintrack.core.domain.common.DomainResult
import com.fintrack.core.domain.model.MercadoPagoConnectionState
import com.fintrack.core.domain.model.MercadoPagoOAuthCallback
import com.fintrack.core.domain.repository.DeviceIdentityPort
import com.fintrack.core.domain.repository.FinTrackBackendPort
import com.fintrack.core.domain.repository.MercadoPagoConnectionPort
import com.fintrack.core.domain.repository.MercadoPagoSyncMetadataPort
import com.fintrack.core.domain.usecase.backend.EnsureDeviceRegisteredUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObserveMercadoPagoConnectionUseCase @Inject constructor(
    private val mercadoPagoConnectionPort: MercadoPagoConnectionPort,
) {
    operator fun invoke(): Flow<MercadoPagoConnectionState> =
        mercadoPagoConnectionPort.observeConnection()
}

@Singleton
class ConnectMercadoPagoUseCase @Inject constructor(
    private val deviceIdentityPort: DeviceIdentityPort,
    private val finTrackBackendPort: FinTrackBackendPort,
    private val ensureDeviceRegisteredUseCase: EnsureDeviceRegisteredUseCase,
) {
    suspend operator fun invoke(appVersion: String? = null): DomainResult<String> {
        when (val ready = ensureDeviceRegisteredUseCase(appVersion = appVersion)) {
            is DomainResult.Error -> return ready
            is DomainResult.Success -> Unit
        }
        val deviceId = deviceIdentityPort.getOrCreateDeviceId()
        return finTrackBackendPort.startMercadoPagoOAuth(deviceId)
    }
}

@Singleton
class HandleMercadoPagoOAuthCallbackUseCase @Inject constructor(
    private val deviceIdentityPort: DeviceIdentityPort,
    private val mercadoPagoConnectionPort: MercadoPagoConnectionPort,
) {
    suspend operator fun invoke(callback: MercadoPagoOAuthCallback): DomainResult<Unit> {
        val localDeviceId = deviceIdentityPort.getOrCreateDeviceId()
        if (callback.deviceId != null && callback.deviceId != localDeviceId) {
            val message = "La autorización no corresponde a este dispositivo"
            mercadoPagoConnectionPort.setError(message)
            return DomainResult.Error(message)
        }
        return if (callback.success) {
            mercadoPagoConnectionPort.setConnected()
            DomainResult.Success(Unit)
        } else {
            val message = callback.reason?.takeIf { it.isNotBlank() }
                ?: "No se pudo conectar Mercado Pago"
            mercadoPagoConnectionPort.setError(message)
            DomainResult.Error(message)
        }
    }
}

@Singleton
class DisconnectMercadoPagoUseCase @Inject constructor(
    private val mercadoPagoConnectionPort: MercadoPagoConnectionPort,
    private val syncMetadataPort: MercadoPagoSyncMetadataPort,
) {
    suspend operator fun invoke() {
        mercadoPagoConnectionPort.setDisconnected()
        syncMetadataPort.clearLastSyncAt()
    }
}
