package com.fintrack.core.domain.classification

import com.fintrack.core.domain.model.ClassificationResult
import com.fintrack.core.domain.model.ClassificationRule
import com.fintrack.core.domain.model.ClassificationSource
import com.fintrack.core.domain.model.MatchType

class RulesEngine {
    fun classify(
        merchantNormalized: String,
        rules: List<ClassificationRule>,
    ): ClassificationResult? {
        val sortedRules = rules.filter { it.isActive }.sortedByDescending { it.priority }

        for (rule in sortedRules) {
            if (matches(merchantNormalized, rule)) {
                return ClassificationResult(
                    categoryId = rule.categoryId,
                    source = ClassificationSource.RULE,
                    confidence = 1.0f,
                    needsReview = false,
                )
            }
        }
        return null
    }

    private fun matches(merchant: String, rule: ClassificationRule): Boolean {
        val pattern = MerchantNormalizer.normalize(rule.pattern)
        return when (rule.matchType) {
            MatchType.EXACT -> merchant == pattern
            MatchType.PREFIX -> merchant.startsWith(pattern)
            MatchType.CONTAINS -> merchant.contains(pattern)
            MatchType.REGEX -> runCatching {
                Regex(pattern, RegexOption.IGNORE_CASE).containsMatchIn(merchant)
            }.getOrDefault(false)
        }
    }
}
