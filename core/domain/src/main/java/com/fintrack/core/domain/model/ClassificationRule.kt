package com.fintrack.core.domain.model

import java.time.Instant

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

data class ClassificationResult(
    val categoryId: Long?,
    val subcategoryId: Long? = null,
    val source: ClassificationSource,
    val confidence: Float? = null,
    val needsReview: Boolean = false,
)
