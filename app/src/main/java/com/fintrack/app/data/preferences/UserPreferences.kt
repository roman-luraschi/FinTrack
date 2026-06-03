package com.fintrack.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fintrack.core.domain.model.DashboardPeriod
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fintrack_prefs")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val defaultAccountId: Flow<Long?> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEFAULT_ACCOUNT]?.takeIf { it > 0 }
    }

    val fuzzyThreshold: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[KEY_FUZZY_THRESHOLD] ?: DEFAULT_FUZZY_THRESHOLD
    }

    val dashboardPeriod: Flow<DashboardPeriod> = context.dataStore.data.map { prefs ->
        when (prefs[KEY_DASHBOARD_PERIOD]) {
            DashboardPeriod.WEEK.name -> DashboardPeriod.WEEK
            else -> DashboardPeriod.MONTH
        }
    }

    val firstLaunchCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_FIRST_LAUNCH] ?: false
    }

    suspend fun setDefaultAccountId(id: Long?) {
        context.dataStore.edit { prefs ->
            if (id != null) prefs[KEY_DEFAULT_ACCOUNT] = id else prefs.remove(KEY_DEFAULT_ACCOUNT)
        }
    }

    suspend fun setFuzzyThreshold(threshold: Float) {
        context.dataStore.edit { prefs ->
            prefs[KEY_FUZZY_THRESHOLD] = threshold.coerceIn(0.5f, 1.0f)
        }
    }

    suspend fun setDashboardPeriod(period: DashboardPeriod) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DASHBOARD_PERIOD] = period.name
        }
    }

    suspend fun setFirstLaunchCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_FIRST_LAUNCH] = completed
        }
    }

    companion object {
        private val KEY_DEFAULT_ACCOUNT = longPreferencesKey("default_account_id")
        private val KEY_FUZZY_THRESHOLD = floatPreferencesKey("fuzzy_threshold")
        private val KEY_DASHBOARD_PERIOD = stringPreferencesKey("dashboard_period")
        private val KEY_FIRST_LAUNCH = booleanPreferencesKey("first_launch_completed")

        const val DEFAULT_FUZZY_THRESHOLD = 0.85f
    }
}
