package com.fintrack.core.domain.classification

import com.fintrack.core.domain.model.ClassificationResult
import com.fintrack.core.domain.model.ClassificationRule
import com.fintrack.core.domain.model.ClassificationSource
import com.fintrack.core.domain.model.LearnedMerchantCategory

class SimilarityEngine(
    private val autoAssignThreshold: Float = DEFAULT_THRESHOLD,
) {
    fun classify(
        merchantNormalized: String,
        rules: List<ClassificationRule>,
        learnedMappings: List<LearnedMerchantCategory>,
    ): ClassificationResult? {
        if (merchantNormalized.isBlank()) return null

        val candidates = buildCandidates(rules, learnedMappings)
        if (candidates.isEmpty()) return null

        var bestScore = 0f
        var bestCategoryId: Long? = null
        var bestSubcategoryId: Long? = null

        for ((pattern, categoryId, subcategoryId) in candidates) {
            val score = similarityRatio(merchantNormalized, pattern)
            if (score > bestScore) {
                bestScore = score
                bestCategoryId = categoryId
                bestSubcategoryId = subcategoryId
            }
        }

        if (bestCategoryId == null || bestScore < SUGGEST_THRESHOLD) return null

        val needsReview = bestScore < autoAssignThreshold
        return ClassificationResult(
            categoryId = bestCategoryId,
            subcategoryId = bestSubcategoryId,
            source = ClassificationSource.SIMILARITY,
            confidence = bestScore,
            needsReview = needsReview,
        )
    }

    private fun buildCandidates(
        rules: List<ClassificationRule>,
        learnedMappings: List<LearnedMerchantCategory>,
    ): List<Triple<String, Long, Long?>> {
        val ruleCandidates = rules
            .filter { it.isActive }
            .map { Triple(MerchantNormalizer.normalize(it.pattern), it.categoryId, null as Long?) }
        val learnedCandidates = learnedMappings
            .filter { it.deletedAt == null }
            .map { Triple(it.merchantNormalized, it.categoryId, it.subcategoryId) }
        return (ruleCandidates + learnedCandidates).distinctBy { it.first }
    }

    internal fun similarityRatio(a: String, b: String): Float {
        if (a.isEmpty() || b.isEmpty()) return 0f
        if (a == b) return 1f
        val distance = levenshteinDistance(a, b)
        val maxLen = maxOf(a.length, b.length)
        return 1f - (distance.toFloat() / maxLen.toFloat())
    }

    private fun levenshteinDistance(a: String, b: String): Int {
        val costs = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            var lastValue = i - 1
            costs[0] = i
            for (j in 1..b.length) {
                val originalValue = costs[j]
                val substitutionCost = if (a[i - 1] == b[j - 1]) lastValue else lastValue + 1
                lastValue = costs[j]
                costs[j] = minOf(
                    costs[j] + 1,
                    costs[j - 1] + 1,
                    substitutionCost,
                )
            }
        }
        return costs[b.length]
    }

    companion object {
        const val DEFAULT_THRESHOLD = 0.85f
        const val SUGGEST_THRESHOLD = 0.70f
    }
}
