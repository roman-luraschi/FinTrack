package com.fintrack.core.domain.model

import java.math.BigDecimal
import java.time.Instant

data class Transaction(
    val id: Long = 0,
    val externalId: String? = null,
    val amount: BigDecimal,
    val type: TransactionType,
    val description: String,
    val merchantNormalized: String,
    val categoryId: Long? = null,
    val subcategoryId: Long? = null,
    val classificationSource: ClassificationSource = ClassificationSource.DEFAULT,
    val classificationConfidence: Float? = null,
    val needsReview: Boolean = false,
    val source: TransactionSource = TransactionSource.MANUAL,
    val accountId: Long,
    val transactionDate: Instant,
    val notes: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant? = null,
)

data class Account(
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val currency: String = "ARS",
    val colorHex: String? = null,
    val isDefault: Boolean = false,
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

data class IngestionBatch(
    val source: TransactionSource,
    val items: List<Transaction>,
)

data class IngestionResult(
    val inserted: Int,
    val updated: Int,
    val skipped: Int,
)
