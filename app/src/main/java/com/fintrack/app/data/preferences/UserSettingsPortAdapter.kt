package com.fintrack.app.data.preferences

import com.fintrack.core.domain.model.DashboardPeriod
import com.fintrack.core.domain.repository.UserSettingsPort
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSettingsPortAdapter @Inject constructor(
    private val userPreferences: UserPreferences,
) : UserSettingsPort {
    override fun observeFuzzyThreshold(): Flow<Float> = userPreferences.fuzzyThreshold

    override fun observeDashboardPeriod(): Flow<DashboardPeriod> = userPreferences.dashboardPeriod
}
