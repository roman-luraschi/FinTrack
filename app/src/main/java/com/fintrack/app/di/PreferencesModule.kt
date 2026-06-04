package com.fintrack.app.di

import com.fintrack.app.data.preferences.UserSettingsPortAdapter
import com.fintrack.core.domain.repository.UserSettingsPort
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferencesModule {
    @Binds
    @Singleton
    abstract fun bindUserSettingsPort(adapter: UserSettingsPortAdapter): UserSettingsPort
}
