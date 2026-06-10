package com.fintrack.app.data.device

import com.fintrack.app.data.preferences.UserPreferences
import com.fintrack.core.domain.repository.DeviceIdentityPort
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceIdentityPortAdapter @Inject constructor(
    private val userPreferences: UserPreferences,
) : DeviceIdentityPort {

    override fun observeDeviceId(): Flow<String?> = userPreferences.deviceId

    override suspend fun getOrCreateDeviceId(): String = userPreferences.getOrCreateDeviceId()
}
