package com.fintrack.app.di

import com.fintrack.app.data.notification.NotificationAccessPortAdapter
import com.fintrack.app.feature.notification.DefaultStatusBarNotificationMapper
import com.fintrack.app.feature.notification.StatusBarNotificationMapper
import com.fintrack.core.domain.repository.NotificationAccessPort
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {
    @Binds
    @Singleton
    abstract fun bindNotificationAccessPort(
        adapter: NotificationAccessPortAdapter,
    ): NotificationAccessPort

    @Binds
    @Singleton
    abstract fun bindStatusBarNotificationMapper(
        impl: DefaultStatusBarNotificationMapper,
    ): StatusBarNotificationMapper
}
