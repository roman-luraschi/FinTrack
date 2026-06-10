package com.fintrack.core.domain.usecase.backend

import com.fintrack.core.domain.common.DomainResult
import com.fintrack.core.domain.repository.DeviceIdentityPort
import com.fintrack.core.domain.repository.FinTrackBackendPort
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PingBackendHealthUseCase @Inject constructor(
    private val finTrackBackendPort: FinTrackBackendPort,
) {
    suspend operator fun invoke(): DomainResult<Boolean> = finTrackBackendPort.pingHealth()
}

@Singleton
class RegisterDeviceUseCase @Inject constructor(
    private val deviceIdentityPort: DeviceIdentityPort,
    private val finTrackBackendPort: FinTrackBackendPort,
) {
    suspend operator fun invoke(
        appVersion: String? = null,
        fcmToken: String? = null,
    ): DomainResult<Unit> {
        val deviceId = deviceIdentityPort.getOrCreateDeviceId()
        return finTrackBackendPort.registerDevice(
            deviceId = deviceId,
            appVersion = appVersion,
            fcmToken = fcmToken,
        )
    }
}

@Singleton
class EnsureDeviceRegisteredUseCase @Inject constructor(
    private val finTrackBackendPort: FinTrackBackendPort,
    private val registerDeviceUseCase: RegisterDeviceUseCase,
) {
    suspend operator fun invoke(appVersion: String? = null): DomainResult<Unit> {
        when (val health = finTrackBackendPort.pingHealth()) {
            is DomainResult.Success -> {
                if (!health.data) {
                    return DomainResult.Error("El backend no respondió correctamente")
                }
            }
            is DomainResult.Error -> return health
        }
        return registerDeviceUseCase(appVersion = appVersion)
    }
}
