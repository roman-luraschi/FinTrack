package com.fintrack.core.domain.classification

import com.fintrack.core.domain.model.ClassificationRule
import com.fintrack.core.domain.model.ClassificationSource
import com.fintrack.core.domain.model.LearnedMerchantCategory
import com.fintrack.core.domain.model.MatchType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class ExpenseClassifierTest {
    private val classifier = ExpenseClassifier()
    private val now = Instant.now()

    @Test
    fun `rules engine matches before learning`() {
        val rules = listOf(rule("SPOTIFY", 6))
        val learned = listOf(
            LearnedMerchantCategory(
                merchantNormalized = "SPOTIFY",
                categoryId = 9L,
                learnedAt = now,
                updatedAt = now,
            ),
        )
        val result = classifier.classify(
            description = "SPOTIFY PREMIUM",
            rules = rules,
            learnedMappings = learned,
            defaultCategoryId = 14L,
        )
        assertEquals(6L, result.categoryId)
        assertEquals(ClassificationSource.RULE, result.source)
    }

    @Test
    fun `learning applies when no rule`() {
        val learned = listOf(
            LearnedMerchantCategory(
                merchantNormalized = "LA FAROLA",
                categoryId = 1L,
                learnedAt = now,
                updatedAt = now,
            ),
        )
        val result = classifier.classify(
            description = "La Farola",
            rules = emptyList(),
            learnedMappings = learned,
            defaultCategoryId = 14L,
        )
        assertEquals(1L, result.categoryId)
        assertEquals(ClassificationSource.LEARNED, result.source)
    }

    @Test
    fun `defaults when no match`() {
        val result = classifier.classify(
            description = "COMERCIO DESCONOCIDO XYZ",
            rules = emptyList(),
            learnedMappings = emptyList(),
            defaultCategoryId = 14L,
        )
        assertEquals(14L, result.categoryId)
        assertEquals(ClassificationSource.DEFAULT, result.source)
    }

    private fun rule(pattern: String, categoryId: Long) = ClassificationRule(
        pattern = pattern,
        matchType = MatchType.CONTAINS,
        categoryId = categoryId,
        createdAt = now,
    )
}
