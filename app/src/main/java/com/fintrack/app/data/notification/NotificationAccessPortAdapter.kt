package com.fintrack.app.data.notification

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import com.fintrack.app.feature.notification.FinTrackNotificationListenerService
import com.fintrack.core.domain.repository.NotificationAccessPort
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationAccessPortAdapter @Inject constructor(
    @ApplicationContext private val context: Context,
) : NotificationAccessPort {

    private val listenerEnabled = MutableStateFlow(isListenerEnabled())

    override fun observeListenerEnabled(): Flow<Boolean> = listenerEnabled.asStateFlow()

    override fun isListenerEnabled(): Boolean {
        val component = ComponentName(context, FinTrackNotificationListenerService::class.java)
        val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(context)
        return enabledPackages.contains(component.packageName)
    }

    override fun refreshListenerState() {
        listenerEnabled.update { isListenerEnabled() }
    }

    override fun openListenerSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
