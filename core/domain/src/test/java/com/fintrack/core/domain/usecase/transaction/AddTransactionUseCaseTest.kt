package com.fintrack.core.domain.usecase.transaction

import com.fintrack.core.domain.classification.ExpenseClassifier
import com.fintrack.core.domain.common.DomainResult
import com.fintrack.core.domain.model.ClassificationRule
import com.fintrack.core.domain.model.DashboardPeriod
import com.fintrack.core.domain.model.LearnedMerchantCategory
import com.fintrack.core.domain.model.Transaction
import com.fintrack.core.domain.model.TransactionFilter
import com.fintrack.core.domain.model.TransactionType
import com.fintrack.core.domain.repository.CategoryRepository
import com.fintrack.core.domain.repository.ClassificationRepository
import com.fintrack.core.domain.repository.TransactionRepository
import com.fintrack.core.domain.repository.UserSettingsPort
import com.fintrack.core.domain.usecase.classification.ClassifyExpenseUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.Instant

class AddTransactionUseCaseTest {

    private val transactionRepository = FakeTransactionRepository()
    private val classifyExpenseUseCase = ClassifyExpenseUseCase(
        FakeClassificationRepository(),
        FakeCategoryRepository(),
        ExpenseClassifier(),
    )
    private val useCase = AddTransactionUseCase(
        transactionRepository,
        classifyExpenseUseCase,
        FakeUserSettingsPort(),
    )

    @Test
    fun invoke_rejectsBlankDescription() = runTest {
        val result = useCase(
            amount = BigDecimal.TEN,
            type = TransactionType.EXPENSE,
            description = "   ",
            accountId = 1L,
            transactionDate = Instant.now(),
        )
        assertTrue(result is DomainResult.Error)
    }

    @Test
    fun invoke_insertsExpenseAndChecksDuplicates() = runTest {
        val result = useCase(
            amount = BigDecimal("150.50"),
            type = TransactionType.EXPENSE,
            description = "Supermercado Dia",
            accountId = 1L,
            transactionDate = Instant.parse("2025-06-01T15:00:00Z"),
        )
        assertTrue(result is DomainResult.Success)
        val data = (result as DomainResult.Success).data
        assertEquals(99L, data.transactionId)
        assertEquals(1, transactionRepository.duplicateCheckCount)
        assertEquals(1, transactionRepository.insertCount)
    }

    private class FakeUserSettingsPort : UserSettingsPort {
        override fun observeFuzzyThreshold(): Flow<Float> = flowOf(0.85f)
        override fun observeDashboardPeriod(): Flow<DashboardPeriod> = flowOf(DashboardPeriod.MONTH)
    }

    private class FakeTransactionRepository : TransactionRepository {
        var duplicateCheckCount = 0
        var insertCount = 0

        override fun observeTransactions(filter: TransactionFilter): Flow<List<Transaction>> =
            flowOf(emptyList())

        override fun observeTransaction(id: Long): Flow<Transaction?> = flowOf(null)
        override suspend fun getTransaction(id: Long): Transaction? = null
        override suspend fun insertTransaction(transaction: Transaction): Long {
            insertCount++
            return 99L
        }

        override suspend fun updateTransaction(
            transaction: Transaction,
            changes: List<com.fintrack.core.domain.model.TransactionChange>,
        ) = Unit

        override suspend fun softDeleteTransaction(id: Long, deletedAt: Instant) = Unit

        override suspend fun findDuplicateCandidates(
            amount: BigDecimal,
            merchantNormalized: String,
            transactionDate: Instant,
            windowMinutes: Long,
        ): List<Transaction> {
            duplicateCheckCount++
            return emptyList()
        }

        override suspend fun getDashboardSummary(
            startDate: Instant,
            endDate: Instant,
        ) = throw UnsupportedOperationException()
    }

    private class FakeClassificationRepository : ClassificationRepository {
        override fun observeRules(): Flow<List<ClassificationRule>> = flowOf(emptyList())
        override fun observeLearnedMappings(): Flow<List<LearnedMerchantCategory>> = flowOf(emptyList())
        override suspend fun getActiveRules(): List<ClassificationRule> = emptyList()
        override suspend fun getLearnedMappings(): List<LearnedMerchantCategory> = emptyList()
        override suspend fun insertRule(rule: ClassificationRule): Long = 0L
        override suspend fun updateRule(rule: ClassificationRule) = Unit
        override suspend fun deleteRule(id: Long) = Unit
        override suspend fun upsertLearnedMapping(mapping: LearnedMerchantCategory): Long = 0L
        override suspend fun softDeleteLearnedMapping(id: Long, deletedAt: Instant) = Unit
    }

    private class FakeCategoryRepository : CategoryRepository {
        override fun observeCategories(): Flow<List<com.fintrack.core.domain.model.Category>> =
            flowOf(emptyList())

        override fun observeRootCategories(): Flow<List<com.fintrack.core.domain.model.Category>> =
            flowOf(
                listOf(
                    com.fintrack.core.domain.model.Category(id = 1L, name = "Otros", sortOrder = 99),
                ),
            )

        override suspend fun getCategory(id: Long): com.fintrack.core.domain.model.Category? = null
        override suspend fun getCategoriesByIds(ids: List<Long>): List<com.fintrack.core.domain.model.Category> =
            emptyList()

        override suspend fun insertCategory(category: com.fintrack.core.domain.model.Category): Long = 0L
        override suspend fun updateCategory(category: com.fintrack.core.domain.model.Category) = Unit
        override suspend fun softDeleteCategory(id: Long, deletedAt: Instant) = Unit
        override suspend fun countTransactionsUsingCategory(categoryId: Long): Int = 0
    }
}
