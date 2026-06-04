package com.fintrack.core.domain.model

import java.math.BigDecimal
import java.time.Instant

data class Transaction(
    val id: Long = 0,
    val externalId: String? = null,
    val amount: BigDecimal,
    val currency: String = "ARS",
    val type: TransactionType,
    val status: TransactionStatus = TransactionStatus.CONFIRMED,
    val description: String,
    val descriptionRaw: String? = null,
    val merchantNormalized: String,
    val categoryId: Long? = null,
    val subcategoryId: Long? = null,
    val classificationSource: ClassificationSource = ClassificationSource.DEFAULT,
    val classificationConfidence: Float? = null,
    val needsReview: Boolean = false,
    val source: TransactionSource = TransactionSource.MANUAL,
    val accountId: Long,
    val transferAccountId: Long? = null,
    val transactionDate: Instant,
    val notes: String? = null,
    val ingestionBatchId: Long? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant? = null,
)

data class TransactionFilter(
    val accountId: Long? = null,
    val categoryId: Long? = null,
    val type: TransactionType? = null,
    val searchQuery: String? = null,
    val startDate: Instant? = null,
    val endDate: Instant? = null,
)

data class TransactionChange(
    val id: Long = 0,
    val transactionId: Long,
    val fieldName: String,
    val oldValue: String?,
    val newValue: String?,
    val changedAt: Instant,
    val changeReason: ChangeReason,
)
