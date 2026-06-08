package com.fintrack.core.domain.usecase.ingestion

import com.fintrack.core.domain.classification.ExpenseClassifier
import com.fintrack.core.domain.common.DomainResult
import com.fintrack.core.domain.model.Category
import com.fintrack.core.domain.model.ClassificationRule
import com.fintrack.core.domain.model.DashboardPeriod
import com.fintrack.core.domain.model.IngestionRequest
import com.fintrack.core.domain.model.IngestionResult
import com.fintrack.core.domain.model.LearnedMerchantCategory
import com.fintrack.core.domain.model.MatchType
import com.fintrack.core.domain.model.TransactionDraft
import com.fintrack.core.domain.model.TransactionSource
import com.fintrack.core.domain.model.TransactionType
import com.fintrack.core.domain.repository.CategoryRepository
import com.fintrack.core.domain.repository.ClassificationRepository
import com.fintrack.core.domain.repository.TransactionIngestionPort
import com.fintrack.core.domain.repository.UserSettingsPort
import com.fintrack.core.domain.usecase.classification.ClassifyExpenseUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class IngestTransactionsUseCaseTest {

    private val ingestionPort = FakeIngestionPort()
    private val classifyExpenseUseCase = ClassifyExpenseUseCase(
        FakeClassificationRepository(),
        FakeCategoryRepository(),
        ExpenseClassifier(),
    )
    private val useCase = IngestTransactionsUseCase(
        ingestionPort,
        classifyExpenseUseCase,
        FakeUserSettingsPort(),
    )

    @Test
    fun invoke_rejectsBlankOperationId() = runTest {
        val result = useCase(
            IngestionRequest(
                operationId = "  ",
                source = TransactionSource.CSV_IMPORT,
                drafts = listOf(FakeDrafts.one()),
            ),
        )
        assertTrue(result is DomainResult.Error)
    }

    @Test
    fun invoke_rejectsEmptyDrafts() = runTest {
        val result = useCase(
            IngestionRequest(
                operationId = "op-1",
                source = TransactionSource.CSV_IMPORT,
                drafts = emptyList(),
            ),
        )
        assertTrue(result is DomainResult.Error)
    }

    @Test
    fun invoke_delegatesToPort() = runTest {
        val result = useCase(
            IngestionRequest(
                operationId = "op-1",
                source = TransactionSource.CSV_IMPORT,
                drafts = listOf(FakeDrafts.one()),
            ),
        )
        assertTrue(result is DomainResult.Success)
        assertEquals(42L, (result as DomainResult.Success).data.batchId)
    }

    @Test
    fun invoke_classifiesExpenseDraftsBeforeIngestion() = runTest {
        useCase(
            IngestionRequest(
                operationId = "op-2",
                source = TransactionSource.CSV_IMPORT,
                drafts = listOf(
                    FakeDrafts.one().copy(
                        description = "MCDONALDS PALERMO",
                        type = TransactionType.EXPENSE,
                    ),
                ),
            ),
        )
        val ingestedDraft = ingestionPort.lastRequest?.drafts?.single()
        assertEquals(1L, ingestedDraft?.categoryId)
    }

    private class FakeIngestionPort : TransactionIngestionPort {
        var lastRequest: IngestionRequest? = null

        override suspend fun ingest(request: IngestionRequest): IngestionResult {
            lastRequest = request
            return IngestionResult(
                batchId = 42L,
                inserted = request.drafts.size,
                updated = 0,
                skipped = 0,
                errors = 0,
                duplicateCandidates = emptyList(),
            )
        }
    }

    private class FakeUserSettingsPort : UserSettingsPort {
        override fun observeFuzzyThreshold(): Flow<Float> = flowOf(0.85f)
        override fun observeDashboardPeriod(): Flow<DashboardPeriod> = flowOf(DashboardPeriod.MONTH)
    }

    private class FakeClassificationRepository : ClassificationRepository {
        private val now = Instant.now()

        override fun observeRules(): Flow<List<ClassificationRule>> = flowOf(emptyList())
        override fun observeLearnedMappings(): Flow<List<LearnedMerchantCategory>> = flowOf(emptyList())

        override suspend fun getActiveRules(): List<ClassificationRule> = listOf(
            ClassificationRule(
                pattern = "MCDONALDS",
                matchType = MatchType.CONTAINS,
                categoryId = 1L,
                createdAt = now,
            ),
        )

        override suspend fun getLearnedMappings(): List<LearnedMerchantCategory> = emptyList()
        override suspend fun insertRule(rule: ClassificationRule): Long = 0L
        override suspend fun updateRule(rule: ClassificationRule) = Unit
        override suspend fun deleteRule(id: Long) = Unit
        override suspend fun upsertLearnedMapping(mapping: LearnedMerchantCategory): Long = 0L
        override suspend fun softDeleteLearnedMapping(id: Long, deletedAt: Instant) = Unit
    }

    private class FakeCategoryRepository : CategoryRepository {
        override fun observeCategories(): Flow<List<Category>> = flowOf(emptyList())
        override fun observeRootCategories(): Flow<List<Category>> = flowOf(
            listOf(Category(id = 14L, name = "Sin clasificar", isSystem = true, sortOrder = 99)),
        )
        override suspend fun getCategory(id: Long): Category? = null
        override suspend fun getCategoriesByIds(ids: List<Long>): List<Category> = emptyList()
        override suspend fun insertCategory(category: Category): Long = 0L
        override suspend fun updateCategory(category: Category) = Unit
        override suspend fun softDeleteCategory(id: Long, deletedAt: Instant) = Unit
        override suspend fun countTransactionsUsingCategory(categoryId: Long): Int = 0
    }
}
