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

data class IngestionBatch(
    val id: Long = 0,
    val operationId: String,
    val source: TransactionSource,
    val status: IngestionBatchStatus,
    val targetAccountId: Long? = null,
    val fileName: String? = null,
    val fileHash: String? = null,
    val recordCount: Int = 0,
    val insertedCount: Int = 0,
    val updatedCount: Int = 0,
    val skippedCount: Int = 0,
    val errorCount: Int = 0,
    val errorSummary: String? = null,
    val startedAt: Instant,
    val completedAt: Instant? = null,
    val createdAt: Instant,
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
    val provenance: ProvenanceDraft,
)

data class TransactionWithProvenance(
    val transaction: Transaction,
    val provenance: TransactionProvenance?,
)

data class Account(
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val currency: String = "ARS",
    val colorHex: String? = null,
    val isDefault: Boolean = false,
    val integrationProvider: IntegrationProvider? = null,
    val externalAccountId: String? = null,
    val notificationListenerEnabled: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant? = null,
)

data class Category(
    val id: Long = 0,
    val name: String,
    val parentId: Long? = null,
    val iconName: String? = null,
    val colorHex: String? = null,
    val isSystem: Boolean = false,
    val sortOrder: Int = 0,
    val deletedAt: Instant? = null,
)

data class ClassificationRule(
    val id: Long = 0,
    val pattern: String,
    val matchType: MatchType,
    val categoryId: Long,
    val priority: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Instant,
)

data class LearnedMerchantCategory(
    val id: Long = 0,
    val merchantNormalized: String,
    val categoryId: Long,
    val subcategoryId: Long? = null,
    val learnedAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant? = null,
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

data class CategoryTotal(
    val categoryId: Long,
    val categoryName: String,
    val total: BigDecimal,
)

data class DashboardSummary(
    val totalExpenses: BigDecimal,
    val totalIncome: BigDecimal,
    val netBalance: BigDecimal,
    val byCategory: List<CategoryTotal>,
    val periodStart: Instant,
    val periodEnd: Instant,
)

data class TransactionFilter(
    val accountId: Long? = null,
    val categoryId: Long? = null,
    val type: TransactionType? = null,
    val searchQuery: String? = null,
    val startDate: Instant? = null,
    val endDate: Instant? = null,
)

data class ClassificationResult(
    val categoryId: Long?,
    val subcategoryId: Long? = null,
    val source: ClassificationSource,
    val confidence: Float? = null,
    val needsReview: Boolean = false,
)

data class IngestionRequest(
    val operationId: String,
    val source: TransactionSource,
    val targetAccountId: Long? = null,
    val fileName: String? = null,
    val fileHash: String? = null,
    val drafts: List<TransactionDraft>,
)

data class IngestionResult(
    val batchId: Long,
    val inserted: Int,
    val updated: Int,
    val skipped: Int,
    val errors: Int,
    val duplicateCandidates: List<Long>,
)
