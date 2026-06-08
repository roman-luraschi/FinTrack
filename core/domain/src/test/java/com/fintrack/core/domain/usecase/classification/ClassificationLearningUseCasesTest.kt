package com.fintrack.core.domain.usecase.classification

import com.fintrack.core.domain.common.DomainResult
import com.fintrack.core.domain.model.ClassificationResult
import com.fintrack.core.domain.model.ChangeReason
import com.fintrack.core.domain.model.ClassificationSource
import com.fintrack.core.domain.model.DashboardSummary
import com.fintrack.core.domain.model.LearnedMerchantCategory
import com.fintrack.core.domain.model.Transaction
import com.fintrack.core.domain.model.TransactionChange
import com.fintrack.core.domain.model.TransactionType
import com.fintrack.core.domain.repository.ClassificationRepository
import com.fintrack.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class ClassificationLearningUseCasesTest {

    private val classificationRepository = FakeClassificationRepository()
    private val transactionRepository = FakeTransactionRepository()
    private val learnUseCase = LearnFromCorrectionUseCase(classificationRepository)
    private val recordCorrectionUseCase = RecordCategoryCorrectionUseCase(
        transactionRepository,
        learnUseCase,
    )

    @Test
    fun learnFromCorrection_upsertsByMerchantNormalized() = runTest {
        val result = learnUseCase(
            merchantNormalized = "LA FAROLA",
            categoryId = 1L,
        )
        assertTrue(result is DomainResult.Success)
        assertEquals(1, classificationRepository.upsertCount)
        assertEquals("LA FAROLA", classificationRepository.lastUpsert?.merchantNormalized)
    }

    @Test
    fun recordCategoryCorrection_updatesTransactionAndLearns() = runTest {
        val now = Instant.now()
        transactionRepository.transaction = Transaction(
            id = 10L,
            amount = java.math.BigDecimal.TEN,
            type = TransactionType.EXPENSE,
            description = "La Farola",
            merchantNormalized = "LA FAROLA",
            categoryId = 14L,
            accountId = 1L,
            transactionDate = now,
            createdAt = now,
            updatedAt = now,
        )

        val result = recordCorrectionUseCase(
            transactionId = 10L,
            newCategoryId = 1L,
        )

        assertTrue(result is DomainResult.Success)
        assertEquals(1L, transactionRepository.transaction?.categoryId)
        assertEquals(ClassificationSource.USER_OVERRIDE, transactionRepository.transaction?.classificationSource)
        assertEquals(1, classificationRepository.upsertCount)
        assertEquals(ChangeReason.RECLASSIFICATION, transactionRepository.lastChanges.first().changeReason)
    }

    private class FakeClassificationRepository : ClassificationRepository {
        var upsertCount = 0
        var lastUpsert: LearnedMerchantCategory? = null

        override fun observeRules(): Flow<List<com.fintrack.core.domain.model.ClassificationRule>> =
            flowOf(emptyList())

        override fun observeLearnedMappings(): Flow<List<LearnedMerchantCategory>> = flowOf(emptyList())
        override suspend fun getActiveRules(): List<com.fintrack.core.domain.model.ClassificationRule> = emptyList()
        override suspend fun getLearnedMappings(): List<LearnedMerchantCategory> = emptyList()
        override suspend fun insertRule(rule: com.fintrack.core.domain.model.ClassificationRule): Long = 0L
        override suspend fun updateRule(rule: com.fintrack.core.domain.model.ClassificationRule) = Unit
        override suspend fun deleteRule(id: Long) = Unit

        override suspend fun upsertLearnedMapping(mapping: LearnedMerchantCategory): Long {
            upsertCount++
            lastUpsert = mapping
            return 1L
        }

        override suspend fun softDeleteLearnedMapping(id: Long, deletedAt: Instant) = Unit
    }

    private class FakeTransactionRepository : TransactionRepository {
        var transaction: Transaction? = null
        var lastChanges: List<TransactionChange> = emptyList()

        override fun observeTransactions(filter: com.fintrack.core.domain.model.TransactionFilter): Flow<List<Transaction>> =
            flowOf(emptyList())

        override fun observeTransaction(id: Long): Flow<Transaction?> = flowOf(transaction)
        override suspend fun getTransaction(id: Long): Transaction? = transaction?.takeIf { it.id == id }
        override suspend fun insertTransaction(transaction: Transaction): Long = 0L

        override suspend fun updateTransaction(
            transaction: Transaction,
            changes: List<TransactionChange>,
        ) {
            this.transaction = transaction
            lastChanges = changes
        }

        override suspend fun softDeleteTransaction(id: Long, deletedAt: Instant) = Unit
        override suspend fun findDuplicateCandidates(
            amount: java.math.BigDecimal,
            merchantNormalized: String,
            transactionDate: Instant,
            windowMinutes: Long,
        ): List<Transaction> = emptyList()

        override suspend fun getDashboardSummary(
            startDate: Instant,
            endDate: Instant,
        ): DashboardSummary = DashboardSummary(
            totalExpenses = java.math.BigDecimal.ZERO,
            totalIncome = java.math.BigDecimal.ZERO,
            netBalance = java.math.BigDecimal.ZERO,
            byCategory = emptyList(),
            periodStart = startDate,
            periodEnd = endDate,
        )
    }
}
