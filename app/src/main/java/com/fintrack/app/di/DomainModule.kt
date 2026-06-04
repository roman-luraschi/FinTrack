package com.fintrack.app.di

import com.fintrack.app.data.preferences.UserSettingsPortAdapter
import com.fintrack.core.domain.classification.ExpenseClassifier
import com.fintrack.core.domain.repository.AccountRepository
import com.fintrack.core.domain.repository.CategoryRepository
import com.fintrack.core.domain.repository.ClassificationRepository
import com.fintrack.core.domain.repository.TransactionIngestionPort
import com.fintrack.core.domain.repository.TransactionRepository
import com.fintrack.core.domain.repository.UserSettingsPort
import com.fintrack.core.domain.usecase.account.AddAccountUseCase
import com.fintrack.core.domain.usecase.account.DeleteAccountUseCase
import com.fintrack.core.domain.usecase.account.GetDefaultAccountUseCase
import com.fintrack.core.domain.usecase.account.ObserveAccountsUseCase
import com.fintrack.core.domain.usecase.account.SetDefaultAccountUseCase
import com.fintrack.core.domain.usecase.account.UpdateAccountUseCase
import com.fintrack.core.domain.usecase.category.AddCategoryUseCase
import com.fintrack.core.domain.usecase.category.DeleteCategoryUseCase
import com.fintrack.core.domain.usecase.category.ObserveCategoriesUseCase
import com.fintrack.core.domain.usecase.category.ObserveRootCategoriesUseCase
import com.fintrack.core.domain.usecase.classification.AddClassificationRuleUseCase
import com.fintrack.core.domain.usecase.classification.ClassifyExpenseUseCase
import com.fintrack.core.domain.usecase.classification.DeleteClassificationRuleUseCase
import com.fintrack.core.domain.usecase.classification.LearnFromCorrectionUseCase
import com.fintrack.core.domain.usecase.classification.ObserveClassificationRulesUseCase
import com.fintrack.core.domain.usecase.classification.ObserveLearnedMappingsUseCase
import com.fintrack.core.domain.usecase.classification.RecordCategoryCorrectionUseCase
import com.fintrack.core.domain.usecase.classification.RevertLearnedMappingUseCase
import com.fintrack.core.domain.usecase.dashboard.GetDashboardSummaryUseCase
import com.fintrack.core.domain.usecase.dashboard.ObserveDashboardSummaryUseCase
import com.fintrack.core.domain.usecase.ingestion.IngestTransactionsUseCase
import com.fintrack.core.domain.usecase.transaction.AddTransactionUseCase
import com.fintrack.core.domain.usecase.transaction.DeleteTransactionUseCase
import com.fintrack.core.domain.usecase.transaction.ObserveTransactionUseCase
import com.fintrack.core.domain.usecase.transaction.ObserveTransactionsUseCase
import com.fintrack.core.domain.usecase.transaction.UpdateTransactionUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainBindingsModule {
    @Binds
    @Singleton
    abstract fun bindUserSettingsPort(adapter: UserSettingsPortAdapter): UserSettingsPort
}

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {
    @Provides
    @Singleton
    fun provideObserveTransactionsUseCase(
        transactionRepository: TransactionRepository,
    ): ObserveTransactionsUseCase = ObserveTransactionsUseCase(transactionRepository)

    @Provides
    @Singleton
    fun provideObserveTransactionUseCase(
        transactionRepository: TransactionRepository,
    ): ObserveTransactionUseCase = ObserveTransactionUseCase(transactionRepository)

    @Provides
    @Singleton
    fun provideAddTransactionUseCase(
        transactionRepository: TransactionRepository,
        classifyExpenseUseCase: ClassifyExpenseUseCase,
        userSettingsPort: UserSettingsPort,
    ): AddTransactionUseCase = AddTransactionUseCase(
        transactionRepository,
        classifyExpenseUseCase,
        userSettingsPort,
    )

    @Provides
    @Singleton
    fun provideUpdateTransactionUseCase(
        transactionRepository: TransactionRepository,
    ): UpdateTransactionUseCase = UpdateTransactionUseCase(transactionRepository)

    @Provides
    @Singleton
    fun provideDeleteTransactionUseCase(
        transactionRepository: TransactionRepository,
    ): DeleteTransactionUseCase = DeleteTransactionUseCase(transactionRepository)

    @Provides
    @Singleton
    fun provideObserveAccountsUseCase(
        accountRepository: AccountRepository,
    ): ObserveAccountsUseCase = ObserveAccountsUseCase(accountRepository)

    @Provides
    @Singleton
    fun provideGetDefaultAccountUseCase(
        accountRepository: AccountRepository,
    ): GetDefaultAccountUseCase = GetDefaultAccountUseCase(accountRepository)

    @Provides
    @Singleton
    fun provideAddAccountUseCase(
        accountRepository: AccountRepository,
    ): AddAccountUseCase = AddAccountUseCase(accountRepository)

    @Provides
    @Singleton
    fun provideUpdateAccountUseCase(
        accountRepository: AccountRepository,
    ): UpdateAccountUseCase = UpdateAccountUseCase(accountRepository)

    @Provides
    @Singleton
    fun provideDeleteAccountUseCase(
        accountRepository: AccountRepository,
    ): DeleteAccountUseCase = DeleteAccountUseCase(accountRepository)

    @Provides
    @Singleton
    fun provideSetDefaultAccountUseCase(
        accountRepository: AccountRepository,
    ): SetDefaultAccountUseCase = SetDefaultAccountUseCase(accountRepository)

    @Provides
    @Singleton
    fun provideObserveCategoriesUseCase(
        categoryRepository: CategoryRepository,
    ): ObserveCategoriesUseCase = ObserveCategoriesUseCase(categoryRepository)

    @Provides
    @Singleton
    fun provideObserveRootCategoriesUseCase(
        categoryRepository: CategoryRepository,
    ): ObserveRootCategoriesUseCase = ObserveRootCategoriesUseCase(categoryRepository)

    @Provides
    @Singleton
    fun provideAddCategoryUseCase(
        categoryRepository: CategoryRepository,
    ): AddCategoryUseCase = AddCategoryUseCase(categoryRepository)

    @Provides
    @Singleton
    fun provideDeleteCategoryUseCase(
        categoryRepository: CategoryRepository,
    ): DeleteCategoryUseCase = DeleteCategoryUseCase(categoryRepository)

    @Provides
    @Singleton
    fun provideClassifyExpenseUseCase(
        classificationRepository: ClassificationRepository,
        categoryRepository: CategoryRepository,
        expenseClassifier: ExpenseClassifier,
    ): ClassifyExpenseUseCase = ClassifyExpenseUseCase(
        classificationRepository,
        categoryRepository,
        expenseClassifier,
    )

    @Provides
    @Singleton
    fun provideObserveClassificationRulesUseCase(
        classificationRepository: ClassificationRepository,
    ): ObserveClassificationRulesUseCase = ObserveClassificationRulesUseCase(classificationRepository)

    @Provides
    @Singleton
    fun provideAddClassificationRuleUseCase(
        classificationRepository: ClassificationRepository,
    ): AddClassificationRuleUseCase = AddClassificationRuleUseCase(classificationRepository)

    @Provides
    @Singleton
    fun provideDeleteClassificationRuleUseCase(
        classificationRepository: ClassificationRepository,
    ): DeleteClassificationRuleUseCase = DeleteClassificationRuleUseCase(classificationRepository)

    @Provides
    @Singleton
    fun provideObserveLearnedMappingsUseCase(
        classificationRepository: ClassificationRepository,
    ): ObserveLearnedMappingsUseCase = ObserveLearnedMappingsUseCase(classificationRepository)

    @Provides
    @Singleton
    fun provideLearnFromCorrectionUseCase(
        classificationRepository: ClassificationRepository,
    ): LearnFromCorrectionUseCase = LearnFromCorrectionUseCase(classificationRepository)

    @Provides
    @Singleton
    fun provideRevertLearnedMappingUseCase(
        classificationRepository: ClassificationRepository,
    ): RevertLearnedMappingUseCase = RevertLearnedMappingUseCase(classificationRepository)

    @Provides
    @Singleton
    fun provideRecordCategoryCorrectionUseCase(
        transactionRepository: TransactionRepository,
        learnFromCorrectionUseCase: LearnFromCorrectionUseCase,
    ): RecordCategoryCorrectionUseCase = RecordCategoryCorrectionUseCase(
        transactionRepository,
        learnFromCorrectionUseCase,
    )

    @Provides
    @Singleton
    fun provideObserveDashboardSummaryUseCase(
        transactionRepository: TransactionRepository,
        categoryRepository: CategoryRepository,
        userSettingsPort: UserSettingsPort,
    ): ObserveDashboardSummaryUseCase = ObserveDashboardSummaryUseCase(
        transactionRepository,
        categoryRepository,
        userSettingsPort,
    )

    @Provides
    @Singleton
    fun provideGetDashboardSummaryUseCase(
        transactionRepository: TransactionRepository,
    ): GetDashboardSummaryUseCase = GetDashboardSummaryUseCase(transactionRepository)

    @Provides
    @Singleton
    fun provideIngestTransactionsUseCase(
        ingestionPort: TransactionIngestionPort,
    ): IngestTransactionsUseCase = IngestTransactionsUseCase(ingestionPort)
}
