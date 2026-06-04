package com.fintrack.core.domain.repository

import com.fintrack.core.domain.model.DashboardPeriod
import kotlinx.coroutines.flow.Flow

interface UserSettingsPort {
    fun observeFuzzyThreshold(): Flow<Float>
    fun observeDashboardPeriod(): Flow<DashboardPeriod>
}
