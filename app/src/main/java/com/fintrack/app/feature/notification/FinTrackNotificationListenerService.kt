package com.fintrack.app.feature.notification

import android.content.ComponentName
import android.content.pm.ApplicationInfo
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.fintrack.app.di.NotificationEntryPoint
import dagger.hilt.android.EntryPointAccessors

class FinTrackNotificationListenerService : NotificationListenerService() {

    private val entryPoint: NotificationEntryPoint by lazy {
        EntryPointAccessors.fromApplication(applicationContext, NotificationEntryPoint::class.java)
    }

    private val mapper: StatusBarNotificationMapper by lazy { entryPoint.statusBarNotificationMapper() }

    private val relevanceFilter by lazy { entryPoint.notificationRelevanceFilter() }

    private val ingestScheduler by lazy { entryPoint.notificationIngestScheduler() }

    override fun onListenerConnected() {
        super.onListenerConnected()
        logDebug("onListenerConnected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        logDebug("onListenerDisconnected")
        requestRebind(ComponentName(this, FinTrackNotificationListenerService::class.java))
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return
        val dto = mapper.toDto(sbn) ?: return
        if (!relevanceFilter.isRelevant(dto)) return
        ingestScheduler.enqueue(dto)
        logDebug("enqueued pkg=${dto.packageName} id=${dto.notificationId} postedAt=${dto.postedAt}")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        if (sbn == null) return
        logDebug("onNotificationRemoved pkg=${sbn.packageName} id=${sbn.id}")
    }

    private fun logDebug(message: String) {
        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            Log.d(TAG, message)
        }
    }

    companion object {
        private const val TAG = "FinTrackNotifListener"
    }
}
