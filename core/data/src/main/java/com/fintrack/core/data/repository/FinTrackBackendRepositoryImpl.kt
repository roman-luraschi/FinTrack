package com.fintrack.core.data.repository

import com.fintrack.core.common.DispatcherProvider
import com.fintrack.core.data.network.FinTrackBackendApi
import com.fintrack.core.data.network.dto.MercadoPagoOAuthStartRequestDto
import com.fintrack.core.data.network.dto.RegisterDeviceRequestDto
import com.fintrack.core.domain.common.DomainResult
import com.fintrack.core.domain.repository.FinTrackBackendPort
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinTrackBackendRepositoryImpl @Inject constructor(
    private val api: FinTrackBackendApi,
    private val dispatchers: DispatcherProvider,
) : FinTrackBackendPort {

    override suspend fun pingHealth(): DomainResult<Boolean> = withContext(dispatchers.io) {
        try {
            val response = api.health()
            DomainResult.Success(response.status.equals("ok", ignoreCase = true))
        } catch (error: Exception) {
            DomainResult.Error(mapNetworkError(error), error)
        }
    }

    override suspend fun registerDevice(
        deviceId: String,
        appVersion: String?,
        fcmToken: String?,
    ): DomainResult<Unit> = withContext(dispatchers.io) {
        try {
            api.registerDevice(
                RegisterDeviceRequestDto(
                    deviceId = deviceId,
                    fcmToken = fcmToken.orEmpty(),
                    appVersion = appVersion.orEmpty(),
                ),
            )
            DomainResult.Success(Unit)
        } catch (error: Exception) {
            DomainResult.Error(mapNetworkError(error), error)
        }
    }

    override suspend fun startMercadoPagoOAuth(deviceId: String): DomainResult<String> =
        withContext(dispatchers.io) {
            try {
                val response = api.startMercadoPagoOAuth(
                    MercadoPagoOAuthStartRequestDto(deviceId = deviceId),
                )
                val url = response.authorizationUrl.trim()
                if (url.isBlank()) {
                    DomainResult.Error("El backend no devolvió una URL de autorización")
                } else {
                    DomainResult.Success(url)
                }
            } catch (error: Exception) {
                DomainResult.Error(mapNetworkError(error), error)
            }
        }

    private fun mapNetworkError(error: Exception): String = when (error) {
        is HttpException -> "Error del backend (${error.code()})"
        is IOException -> "Sin conexión al backend"
        else -> error.message ?: "Error de red"
    }
}
