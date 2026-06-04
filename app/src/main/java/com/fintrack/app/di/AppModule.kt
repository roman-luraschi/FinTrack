package com.fintrack.app.di

import com.fintrack.core.common.DefaultDispatcherProvider
import com.fintrack.core.common.DispatcherProvider
import com.fintrack.core.domain.classification.ExpenseClassifier
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()

    @Provides
    @Singleton
    fun provideExpenseClassifier(): ExpenseClassifier = ExpenseClassifier()
}
