package com.fintrack.app.data.security

import com.fintrack.app.data.preferences.UserPreferences
import com.fintrack.core.domain.model.BiometricAvailability
import com.fintrack.core.domain.repository.BiometricLockPort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricLockPortAdapter @Inject constructor(
    private val capabilityChecker: BiometricCapabilityChecker,
) : BiometricLockPort {
    private val lockEnabled = MutableStateFlow(isLockAvailable())

    override fun observeLockEnabled(): Flow<Boolean> = lockEnabled.asStateFlow()

    override fun refreshLockState() {
        lockEnabled.update { isLockAvailable() }
    }

    private fun isLockAvailable(): Boolean =
        capabilityChecker.check() == BiometricAvailability.Ready

    override suspend fun setLockEnabled(enabled: Boolean) = Unit

    override fun checkAvailability(): BiometricAvailability = capabilityChecker.check()
}
