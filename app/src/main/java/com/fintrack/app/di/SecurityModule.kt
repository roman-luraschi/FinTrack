package com.fintrack.app.di

import com.fintrack.app.data.security.BiometricLockPortAdapter
import com.fintrack.core.domain.repository.BiometricLockPort
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SecurityModule {
    @Binds
    @Singleton
    abstract fun bindBiometricLockPort(adapter: BiometricLockPortAdapter): BiometricLockPort
}
