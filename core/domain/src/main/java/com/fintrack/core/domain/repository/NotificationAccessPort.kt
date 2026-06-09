package com.fintrack.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface NotificationAccessPort {
    fun observeListenerEnabled(): Flow<Boolean>
    fun isListenerEnabled(): Boolean
    fun refreshListenerState()
    fun openListenerSettings()
}
