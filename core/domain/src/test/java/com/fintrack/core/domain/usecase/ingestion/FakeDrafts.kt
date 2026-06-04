package com.fintrack.core.domain.usecase.ingestion

import com.fintrack.core.domain.model.IntegrationProvider
import com.fintrack.core.domain.model.ParseStatus
import com.fintrack.core.domain.model.ProvenanceDraft
import com.fintrack.core.domain.model.TransactionDraft
import com.fintrack.core.domain.model.TransactionSource
import com.fintrack.core.domain.model.TransactionType
import java.math.BigDecimal
import java.time.Instant

object FakeDrafts {
    fun one(): TransactionDraft = TransactionDraft(
        externalId = "ext-1",
        amount = BigDecimal("100.00"),
        type = TransactionType.EXPENSE,
        description = "Test",
        source = TransactionSource.CSV_IMPORT,
        accountId = 1L,
        transactionDate = Instant.parse("2025-06-01T12:00:00Z"),
        provenance = ProvenanceDraft(
            integrationProvider = IntegrationProvider.UNKNOWN,
            rawPayload = "{}",
            payloadFormat = "json",
            parseStatus = ParseStatus.SUCCESS,
            capturedAt = Instant.now(),
        ),
    )
}
