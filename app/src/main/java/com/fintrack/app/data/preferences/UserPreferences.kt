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
import com.fintrack.core.domain.model.MercadoPagoConnectionState
import com.fintrack.core.domain.model.MercadoPagoConnectionStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.UUID
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

    val biometricLockEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_BIOMETRIC_LOCK] ?: false
    }

    val movementAlertEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_MOVEMENT_ALERT] ?: true
    }

    val deviceId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEVICE_ID]?.takeIf { it.isNotBlank() }
    }

    val mercadoPagoConnection: Flow<MercadoPagoConnectionState> = context.dataStore.data.map { prefs ->
        val status = prefs[KEY_MP_CONNECTION_STATUS]
            ?.let { runCatching { MercadoPagoConnectionStatus.valueOf(it) }.getOrNull() }
            ?: MercadoPagoConnectionStatus.DISCONNECTED
        MercadoPagoConnectionState(
            status = status,
            errorMessage = prefs[KEY_MP_CONNECTION_ERROR],
        )
    }

    suspend fun getOrCreateDeviceId(): String {
        val existing = deviceId.first()
        if (existing != null) return existing
        val generated = UUID.randomUUID().toString()
        context.dataStore.edit { prefs ->
            prefs[KEY_DEVICE_ID] = generated
        }
        return generated
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

    suspend fun setBiometricLockEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BIOMETRIC_LOCK] = enabled
        }
    }

    suspend fun setMovementAlertEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_MOVEMENT_ALERT] = enabled
        }
    }

    suspend fun setMercadoPagoConnection(status: MercadoPagoConnectionStatus, errorMessage: String? = null) {
        context.dataStore.edit { prefs ->
            prefs[KEY_MP_CONNECTION_STATUS] = status.name
            if (errorMessage.isNullOrBlank()) {
                prefs.remove(KEY_MP_CONNECTION_ERROR)
            } else {
                prefs[KEY_MP_CONNECTION_ERROR] = errorMessage
            }
        }
    }

    suspend fun getMercadoPagoLastSyncAt(): Instant? {
        val epochMillis = context.dataStore.data.map { prefs ->
            prefs[KEY_MP_LAST_SYNC_AT]
        }.first() ?: return null
        return Instant.ofEpochMilli(epochMillis)
    }

    suspend fun setMercadoPagoLastSyncAt(instant: Instant) {
        context.dataStore.edit { prefs ->
            prefs[KEY_MP_LAST_SYNC_AT] = instant.toEpochMilli()
        }
    }

    suspend fun clearMercadoPagoLastSyncAt() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_MP_LAST_SYNC_AT)
        }
    }

    companion object {
        private val KEY_DEFAULT_ACCOUNT = longPreferencesKey("default_account_id")
        private val KEY_FUZZY_THRESHOLD = floatPreferencesKey("fuzzy_threshold")
        private val KEY_DASHBOARD_PERIOD = stringPreferencesKey("dashboard_period")
        private val KEY_FIRST_LAUNCH = booleanPreferencesKey("first_launch_completed")
        private val KEY_BIOMETRIC_LOCK = booleanPreferencesKey("biometric_lock_enabled")
        private val KEY_MOVEMENT_ALERT = booleanPreferencesKey("movement_alert_enabled")
        private val KEY_DEVICE_ID = stringPreferencesKey("device_id")
        private val KEY_MP_CONNECTION_STATUS = stringPreferencesKey("mp_connection_status")
        private val KEY_MP_CONNECTION_ERROR = stringPreferencesKey("mp_connection_error")
        private val KEY_MP_LAST_SYNC_AT = longPreferencesKey("mp_last_sync_at")

        const val DEFAULT_FUZZY_THRESHOLD = 0.85f
    }
}
