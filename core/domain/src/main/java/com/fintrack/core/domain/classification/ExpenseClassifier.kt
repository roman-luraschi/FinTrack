package com.fintrack.core.domain.classification

import com.fintrack.core.domain.model.ClassificationResult
import com.fintrack.core.domain.model.ClassificationRule
import com.fintrack.core.domain.model.ClassificationSource
import com.fintrack.core.domain.model.LearnedMerchantCategory

class ExpenseClassifier(
    private val rulesEngine: RulesEngine = RulesEngine(),
    private val learningEngine: LearningEngine = LearningEngine(),
    private val similarityEngine: SimilarityEngine = SimilarityEngine(),
) {
    fun classify(
        description: String,
        rules: List<ClassificationRule>,
        learnedMappings: List<LearnedMerchantCategory>,
        defaultCategoryId: Long?,
        fuzzyThreshold: Float = SimilarityEngine.DEFAULT_THRESHOLD,
    ): ClassificationResult {
        val merchantNormalized = MerchantNormalizer.normalize(description)

        rulesEngine.classify(merchantNormalized, rules)?.let { return it }
        learningEngine.classify(merchantNormalized, learnedMappings)?.let { return it }

        SimilarityEngine(fuzzyThreshold)
            .classify(merchantNormalized, rules, learnedMappings)
            ?.let { return it }

        return ClassificationResult(
            categoryId = defaultCategoryId,
            source = ClassificationSource.DEFAULT,
            confidence = null,
            needsReview = defaultCategoryId == null,
        )
    }
}
