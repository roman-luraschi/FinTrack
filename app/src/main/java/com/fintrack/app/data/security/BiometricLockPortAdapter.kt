package com.fintrack.app.data.security

import com.fintrack.app.data.preferences.UserPreferences
import com.fintrack.core.domain.model.BiometricAvailability
import com.fintrack.core.domain.repository.BiometricLockPort
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricLockPortAdapter @Inject constructor(
    private val userPreferences: UserPreferences,
    private val capabilityChecker: BiometricCapabilityChecker,
) : BiometricLockPort {
    override fun observeLockEnabled(): Flow<Boolean> = userPreferences.biometricLockEnabled

    override suspend fun setLockEnabled(enabled: Boolean) {
        userPreferences.setBiometricLockEnabled(enabled)
    }

    override fun checkAvailability(): BiometricAvailability = capabilityChecker.check()
}
