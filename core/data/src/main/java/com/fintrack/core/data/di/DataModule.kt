package com.fintrack.core.data.di

import com.fintrack.core.data.notification.NotificationEnabledAccountsCache
import com.fintrack.core.data.notification.NotificationEnabledAccountsProvider
import com.fintrack.core.data.notification.NotificationRelevanceFilter
import com.fintrack.core.data.notification.NotificationRelevanceFilterImpl
import com.fintrack.core.data.repository.AccountRepositoryImpl
import com.fintrack.core.data.repository.CategoryRepositoryImpl
import com.fintrack.core.data.repository.ClassificationRepositoryImpl
import com.fintrack.core.data.mercadopago.MercadoPagoSyncPortImpl
import com.fintrack.core.data.repository.FinTrackBackendRepositoryImpl
import com.fintrack.core.data.repository.TransactionIngestionPortImpl
import com.fintrack.core.data.repository.TransactionRepositoryImpl
import com.fintrack.core.domain.repository.AccountRepository
import com.fintrack.core.domain.repository.CategoryRepository
import com.fintrack.core.domain.repository.ClassificationRepository
import com.fintrack.core.domain.repository.FinTrackBackendPort
import com.fintrack.core.domain.repository.MercadoPagoSyncPort
import com.fintrack.core.domain.repository.TransactionIngestionPort
import com.fintrack.core.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
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
    abstract fun bindTransactionIngestionPort(impl: TransactionIngestionPortImpl): TransactionIngestionPort

    @Binds
    @Singleton
    abstract fun bindNotificationEnabledAccountsProvider(
        impl: NotificationEnabledAccountsCache,
    ): NotificationEnabledAccountsProvider

    @Binds
    @Singleton
    abstract fun bindNotificationRelevanceFilter(
        impl: NotificationRelevanceFilterImpl,
    ): NotificationRelevanceFilter

    @Binds
    @Singleton
    abstract fun bindFinTrackBackendPort(
        impl: FinTrackBackendRepositoryImpl,
    ): FinTrackBackendPort

    @Binds
    @Singleton
    abstract fun bindMercadoPagoSyncPort(
        impl: MercadoPagoSyncPortImpl,
    ): MercadoPagoSyncPort
}
