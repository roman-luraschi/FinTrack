package com.fintrack.app.data.mercadopago

import com.fintrack.app.data.preferences.UserPreferences
import com.fintrack.core.domain.model.MercadoPagoConnectionState
import com.fintrack.core.domain.model.MercadoPagoConnectionStatus
import com.fintrack.core.domain.repository.MercadoPagoConnectionPort
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MercadoPagoConnectionPortAdapter @Inject constructor(
    private val userPreferences: UserPreferences,
) : MercadoPagoConnectionPort {

    override fun observeConnection(): Flow<MercadoPagoConnectionState> =
        userPreferences.mercadoPagoConnection

    override suspend fun setConnected() {
        userPreferences.setMercadoPagoConnection(MercadoPagoConnectionStatus.CONNECTED)
    }

    override suspend fun setDisconnected() {
        userPreferences.setMercadoPagoConnection(MercadoPagoConnectionStatus.DISCONNECTED)
    }

    override suspend fun setError(message: String) {
        userPreferences.setMercadoPagoConnection(
            status = MercadoPagoConnectionStatus.ERROR,
            errorMessage = message,
        )
    }
}
