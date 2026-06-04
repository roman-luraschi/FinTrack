package com.fintrack.core.domain.repository

import com.fintrack.core.domain.model.BiometricAvailability
import kotlinx.coroutines.flow.Flow

interface BiometricLockPort {
    fun observeLockEnabled(): Flow<Boolean>
    suspend fun setLockEnabled(enabled: Boolean)
    fun checkAvailability(): BiometricAvailability
    fun refreshLockState()
}
