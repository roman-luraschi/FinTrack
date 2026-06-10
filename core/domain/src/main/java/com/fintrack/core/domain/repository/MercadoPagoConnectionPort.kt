package com.fintrack.core.domain.repository

import com.fintrack.core.domain.model.MercadoPagoConnectionState
import kotlinx.coroutines.flow.Flow

interface MercadoPagoConnectionPort {
    fun observeConnection(): Flow<MercadoPagoConnectionState>
    suspend fun setConnected()
    suspend fun setDisconnected()
    suspend fun setError(message: String)
}
