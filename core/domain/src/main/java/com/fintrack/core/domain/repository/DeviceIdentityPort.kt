package com.fintrack.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface DeviceIdentityPort {
    fun observeDeviceId(): Flow<String?>
    suspend fun getOrCreateDeviceId(): String
}
