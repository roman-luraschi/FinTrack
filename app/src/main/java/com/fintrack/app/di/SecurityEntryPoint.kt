package com.fintrack.app.di

import com.fintrack.app.data.security.BiometricPromptAuthenticator
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SecurityEntryPoint {
    fun biometricPromptAuthenticator(): BiometricPromptAuthenticator
}
