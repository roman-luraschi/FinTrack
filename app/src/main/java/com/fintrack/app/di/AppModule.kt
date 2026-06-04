package com.fintrack.app.di

import com.fintrack.app.data.repository.AccountRepositoryImpl
import com.fintrack.app.data.repository.CategoryRepositoryImpl
import com.fintrack.app.data.repository.ClassificationRepositoryImpl
import com.fintrack.app.data.repository.TransactionIngestionPortStub
import com.fintrack.app.data.repository.TransactionRepositoryImpl
import com.fintrack.core.common.DefaultDispatcherProvider
import com.fintrack.core.common.DispatcherProvider
import com.fintrack.core.domain.classification.ExpenseClassifier
import com.fintrack.core.domain.repository.AccountRepository
import com.fintrack.core.domain.repository.CategoryRepository
import com.fintrack.core.domain.repository.ClassificationRepository
import com.fintrack.core.domain.repository.TransactionIngestionPort
import com.fintrack.core.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindClassificationRepository(impl: ClassificationRepositoryImpl): ClassificationRepository

    @Binds
    @Singleton
    abstract fun bindTransactionIngestionPort(impl: TransactionIngestionPortStub): TransactionIngestionPort
}

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
