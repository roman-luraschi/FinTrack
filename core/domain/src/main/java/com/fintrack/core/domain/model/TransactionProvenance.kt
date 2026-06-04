package com.fintrack.core.domain.model

import java.time.Instant

data class TransactionProvenance(
    val transactionId: Long,
    val integrationProvider: IntegrationProvider,
    val providerCode: String? = null,
    val rawPayload: String,
    val payloadFormat: String,
    val parseStatus: ParseStatus,
    val parserVersion: String,
    val dedupMatchType: DedupMatchType = DedupMatchType.NONE,
    val dedupMatchedTransactionId: Long? = null,
    val weakDedupKey: String? = null,
    val capturedAt: Instant,
    val metadataJson: String? = null,
)
