package com.fintrack.core.data.mapper

import com.fintrack.core.domain.model.DedupMatchType
import com.fintrack.core.domain.model.IntegrationProvider
import com.fintrack.core.domain.model.ParseStatus
import com.fintrack.core.domain.model.ProvenanceDraft
import com.fintrack.core.domain.model.TransactionDraft
import com.fintrack.core.domain.model.TransactionSource
import com.fintrack.core.domain.model.TransactionStatus
import com.fintrack.core.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.Instant

class TransactionDraftMapperTest {

    @Test
    fun `toTransactionWithProvenance normalizes merchant and sets batch metadata`() {
        val now = Instant.parse("2026-06-03T12:00:00Z")
        val draft = TransactionDraft(
            externalId = "mp-123",
            amount = BigDecimal("1500.50"),
            type = TransactionType.EXPENSE,
            description = "  Café La Flor  ",
            source = TransactionSource.MERCADO_PAGO_API,
            accountId = 1L,
            transactionDate = now,
            status = TransactionStatus.PENDING,
            provenance = ProvenanceDraft(
                integrationProvider = IntegrationProvider.MERCADO_PAGO,
                rawPayload = """{"id":"mp-123"}""",
                payloadFormat = "json",
                parseStatus = ParseStatus.SUCCESS,
                capturedAt = now,
            ),
        )

        val (transaction, provenance) = draft.toTransactionWithProvenance(
            ingestionBatchId = 99L,
            now = now,
            dedupMatchType = DedupMatchType.STRONG,
            dedupMatchedTransactionId = 5L,
        )

        assertEquals(99L, transaction.ingestionBatchId)
        assertEquals("CAFE LA FLOR", transaction.merchantNormalized)
        assertEquals("Café La Flor", transaction.description)
        assertEquals(DedupMatchType.STRONG, provenance.dedupMatchType)
        assertEquals(5L, provenance.dedupMatchedTransactionId)
        assertEquals(PARSER_VERSION, provenance.parserVersion)
    }

    @Test
    fun `buildWeakDedupKey is stable for same inputs`() {
        val amount = BigDecimal("100.00")
        val merchant = "SUPERMERCADO"
        val date = Instant.parse("2026-06-03T15:30:00Z")

        val key1 = buildWeakDedupKey(amount, merchant, date)
        val key2 = buildWeakDedupKey(amount, merchant, date)

        assertEquals(key1, key2)
        assertTrue(key1.contains("100.00"))
    }
}
