package com.fintrack.core.domain.usecase.ingestion

import com.fintrack.core.domain.common.DomainResult
import com.fintrack.core.domain.model.IngestionRequest
import com.fintrack.core.domain.model.IngestionResult
import com.fintrack.core.domain.model.TransactionSource
import com.fintrack.core.domain.repository.TransactionIngestionPort
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.Instant

class IngestTransactionsUseCaseTest {

    private val useCase = IngestTransactionsUseCase(FakeIngestionPort())

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

    private class FakeIngestionPort : TransactionIngestionPort {
        override suspend fun ingest(request: IngestionRequest): IngestionResult =
            IngestionResult(
                batchId = 42L,
                inserted = request.drafts.size,
                updated = 0,
                skipped = 0,
                errors = 0,
                duplicateCandidates = emptyList(),
            )
    }
}
