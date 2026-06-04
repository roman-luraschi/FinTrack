package com.fintrack.app.di

import com.fintrack.core.domain.classification.ExpenseClassifier
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ClassificationModule {
    @Provides
    @Singleton
    fun provideExpenseClassifier(): ExpenseClassifier = ExpenseClassifier()
}
