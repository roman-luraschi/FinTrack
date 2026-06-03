package com.fintrack.core.domain.classification

import com.fintrack.core.domain.model.ClassificationResult
import com.fintrack.core.domain.model.ClassificationSource
import com.fintrack.core.domain.model.LearnedMerchantCategory

class LearningEngine {
    fun classify(
        merchantNormalized: String,
        learnedMappings: List<LearnedMerchantCategory>,
    ): ClassificationResult? {
        val match = learnedMappings.firstOrNull {
            it.deletedAt == null && it.merchantNormalized == merchantNormalized
        } ?: return null

        return ClassificationResult(
            categoryId = match.categoryId,
            subcategoryId = match.subcategoryId,
            source = ClassificationSource.LEARNED,
            confidence = 1.0f,
            needsReview = false,
        )
    }
}
