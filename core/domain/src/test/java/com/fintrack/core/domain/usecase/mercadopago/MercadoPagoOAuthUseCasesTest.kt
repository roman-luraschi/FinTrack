package com.fintrack.core.domain.usecase.mercadopago

import com.fintrack.core.domain.common.DomainResult
import com.fintrack.core.domain.model.MercadoPagoConnectionState
import com.fintrack.core.domain.model.MercadoPagoConnectionStatus
import com.fintrack.core.domain.model.MercadoPagoOAuthCallback
import com.fintrack.core.domain.repository.DeviceIdentityPort
import com.fintrack.core.domain.repository.FinTrackBackendPort
import com.fintrack.core.domain.repository.MercadoPagoConnectionPort
import com.fintrack.core.domain.repository.MercadoPagoSyncMetadataPort
import com.fintrack.core.domain.usecase.backend.EnsureDeviceRegisteredUseCase
import com.fintrack.core.domain.usecase.backend.RegisterDeviceUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class MercadoPagoOAuthUseCasesTest {

    @Test
    fun `connect returns authorization url when device is registered`() = runTest {
        val backend = FakeFinTrackBackendPort(authorizationUrl = "https://auth.mercadopago.com/start")
        val devicePort = FakeDeviceIdentityPort("device-1")
        val ensureDeviceRegisteredUseCase = EnsureDeviceRegisteredUseCase(
            finTrackBackendPort = backend,
            registerDeviceUseCase = RegisterDeviceUseCase(devicePort, backend),
        )
        val useCase = ConnectMercadoPagoUseCase(
            deviceIdentityPort = devicePort,
            finTrackBackendPort = backend,
            ensureDeviceRegisteredUseCase = ensureDeviceRegisteredUseCase,
        )

        val result = useCase(appVersion = "1.0.0")

        assertTrue(result is DomainResult.Success)
        assertEquals("https://auth.mercadopago.com/start", (result as DomainResult.Success).data)
        assertEquals("device-1", backend.lastOAuthDeviceId)
    }

    @Test
    fun `handle callback marks connected on success`() = runTest {
        val connectionPort = FakeMercadoPagoConnectionPort()
        val useCase = HandleMercadoPagoOAuthCallbackUseCase(
            deviceIdentityPort = FakeDeviceIdentityPort("device-1"),
            mercadoPagoConnectionPort = connectionPort,
        )

        val result = useCase(
            MercadoPagoOAuthCallback(
                success = true,
                deviceId = "device-1",
                reason = null,
            ),
        )

        assertTrue(result is DomainResult.Success)
        assertEquals(MercadoPagoConnectionStatus.CONNECTED, connectionPort.lastStatus)
    }

    @Test
    fun `handle callback rejects mismatched device id`() = runTest {
        val connectionPort = FakeMercadoPagoConnectionPort()
        val useCase = HandleMercadoPagoOAuthCallbackUseCase(
            deviceIdentityPort = FakeDeviceIdentityPort("device-1"),
            mercadoPagoConnectionPort = connectionPort,
        )

        val result = useCase(
            MercadoPagoOAuthCallback(
                success = true,
                deviceId = "other-device",
                reason = null,
            ),
        )

        assertTrue(result is DomainResult.Error)
        assertEquals(MercadoPagoConnectionStatus.ERROR, connectionPort.lastStatus)
    }

    @Test
    fun `handle callback stores error reason on failure`() = runTest {
        val connectionPort = FakeMercadoPagoConnectionPort()
        val useCase = HandleMercadoPagoOAuthCallbackUseCase(
            deviceIdentityPort = FakeDeviceIdentityPort("device-1"),
            mercadoPagoConnectionPort = connectionPort,
        )

        val result = useCase(
            MercadoPagoOAuthCallback(
                success = false,
                deviceId = "device-1",
                reason = "access_denied",
            ),
        )

        assertTrue(result is DomainResult.Error)
        assertEquals(MercadoPagoConnectionStatus.ERROR, connectionPort.lastStatus)
        assertEquals("access_denied", connectionPort.lastErrorMessage)
    }

    @Test
    fun `disconnect clears connection status`() = runTest {
        val connectionPort = FakeMercadoPagoConnectionPort()
        val metadataPort = FakeMercadoPagoSyncMetadataPort()
        val useCase = DisconnectMercadoPagoUseCase(connectionPort, metadataPort)

        useCase()

        assertEquals(MercadoPagoConnectionStatus.DISCONNECTED, connectionPort.lastStatus)
        assertTrue(metadataPort.cleared)
    }

    private class FakeDeviceIdentityPort(
        private val deviceId: String,
    ) : DeviceIdentityPort {
        override fun observeDeviceId(): Flow<String?> = MutableStateFlow(deviceId)
        override suspend fun getOrCreateDeviceId(): String = deviceId
    }

    private class FakeFinTrackBackendPort(
        private val authorizationUrl: String,
    ) : FinTrackBackendPort {
        var lastOAuthDeviceId: String? = null

        override suspend fun pingHealth(): DomainResult<Boolean> =
            DomainResult.Success(true)

        override suspend fun registerDevice(
            deviceId: String,
            appVersion: String?,
            fcmToken: String?,
        ): DomainResult<Unit> = DomainResult.Success(Unit)

        override suspend fun startMercadoPagoOAuth(deviceId: String): DomainResult<String> {
            lastOAuthDeviceId = deviceId
            return DomainResult.Success(authorizationUrl)
        }
    }

    private class FakeMercadoPagoSyncMetadataPort : MercadoPagoSyncMetadataPort {
        var cleared = false

        override suspend fun getLastSyncAt(): Instant? = null
        override suspend fun setLastSyncAt(instant: Instant) = Unit
        override suspend fun clearLastSyncAt() {
            cleared = true
        }
    }

    private class FakeMercadoPagoConnectionPort : MercadoPagoConnectionPort {
        var lastStatus: MercadoPagoConnectionStatus? = null
        var lastErrorMessage: String? = null

        override fun observeConnection(): Flow<MercadoPagoConnectionState> =
            MutableStateFlow(MercadoPagoConnectionState())

        override suspend fun setConnected() {
            lastStatus = MercadoPagoConnectionStatus.CONNECTED
        }

        override suspend fun setDisconnected() {
            lastStatus = MercadoPagoConnectionStatus.DISCONNECTED
        }

        override suspend fun setError(message: String) {
            lastStatus = MercadoPagoConnectionStatus.ERROR
            lastErrorMessage = message
        }
    }
}
