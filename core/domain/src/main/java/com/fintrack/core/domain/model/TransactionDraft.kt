package com.fintrack.core.domain.model

import java.math.BigDecimal
import java.time.Instant

data class TransactionDraft(
    val externalId: String?,
    val amount: BigDecimal,
    val currency: String = "ARS",
    val type: TransactionType,
    val description: String,
    val descriptionRaw: String? = null,
    val source: TransactionSource,
    val accountId: Long,
    val transferAccountId: Long? = null,
    val transactionDate: Instant,
    val status: TransactionStatus = TransactionStatus.PENDING,
    val categoryId: Long? = null,
    val subcategoryId: Long? = null,
    val classificationSource: ClassificationSource? = null,
    val classificationConfidence: Float? = null,
    val classificationNeedsReview: Boolean? = null,
    val provenance: ProvenanceDraft,
)

data class ProvenanceDraft(
    val integrationProvider: IntegrationProvider,
    val providerCode: String? = null,
    val rawPayload: String,
    val payloadFormat: String,
    val parseStatus: ParseStatus,
    val capturedAt: Instant,
    val metadataJson: String? = null,
)

data class TransactionWithProvenance(
    val transaction: Transaction,
    val provenance: TransactionProvenance?,
)
