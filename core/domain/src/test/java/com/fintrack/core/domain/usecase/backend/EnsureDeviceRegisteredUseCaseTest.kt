package com.fintrack.core.domain.usecase.backend

import com.fintrack.core.domain.common.DomainResult
import com.fintrack.core.domain.repository.DeviceIdentityPort
import com.fintrack.core.domain.repository.FinTrackBackendPort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class EnsureDeviceRegisteredUseCaseTest {

    @Test
    fun `registers device when backend is healthy`() = runTest {
        val backend = FakeFinTrackBackendPort(health = DomainResult.Success(true))
        val useCase = EnsureDeviceRegisteredUseCase(
            finTrackBackendPort = backend,
            registerDeviceUseCase = RegisterDeviceUseCase(
                deviceIdentityPort = FakeDeviceIdentityPort("device-abc"),
                finTrackBackendPort = backend,
            ),
        )

        val result = useCase(appVersion = "1.0.0")

        assertTrue(result is DomainResult.Success)
        assertTrue(backend.registerCalled)
    }

    @Test
    fun `fails when backend health check fails`() = runTest {
        val backend = FakeFinTrackBackendPort(
            health = DomainResult.Error("Sin conexión al backend"),
        )
        val useCase = EnsureDeviceRegisteredUseCase(
            finTrackBackendPort = backend,
            registerDeviceUseCase = RegisterDeviceUseCase(
                deviceIdentityPort = FakeDeviceIdentityPort("device-abc"),
                finTrackBackendPort = backend,
            ),
        )

        val result = useCase()

        assertTrue(result is DomainResult.Error)
    }

    private class FakeDeviceIdentityPort(
        private val id: String,
    ) : DeviceIdentityPort {
        override fun observeDeviceId(): Flow<String?> = flowOf(id)
        override suspend fun getOrCreateDeviceId(): String = id
    }

    private class FakeFinTrackBackendPort(
        private val health: DomainResult<Boolean>,
    ) : FinTrackBackendPort {
        var registerCalled = false

        override suspend fun pingHealth(): DomainResult<Boolean> = health

        override suspend fun registerDevice(
            deviceId: String,
            appVersion: String?,
            fcmToken: String?,
        ): DomainResult<Unit> {
            registerCalled = true
            return DomainResult.Success(Unit)
        }

        override suspend fun startMercadoPagoOAuth(deviceId: String): DomainResult<String> =
            DomainResult.Error("not used in test")
    }
}
