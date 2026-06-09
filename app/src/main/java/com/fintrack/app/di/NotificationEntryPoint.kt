package com.fintrack.app.di

import com.fintrack.app.feature.notification.NotificationIngestScheduler
import com.fintrack.app.feature.notification.StatusBarNotificationMapper
import com.fintrack.core.data.notification.NotificationRelevanceFilter
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NotificationEntryPoint {
    fun statusBarNotificationMapper(): StatusBarNotificationMapper
    fun notificationRelevanceFilter(): NotificationRelevanceFilter
    fun notificationIngestScheduler(): NotificationIngestScheduler
}
