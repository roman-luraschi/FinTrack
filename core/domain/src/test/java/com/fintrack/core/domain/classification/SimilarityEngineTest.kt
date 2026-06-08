package com.fintrack.core.domain.classification

import com.fintrack.core.domain.model.ClassificationRule
import com.fintrack.core.domain.model.ClassificationSource
import com.fintrack.core.domain.model.MatchType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class SimilarityEngineTest {
    private val now = Instant.now()

    @Test
    fun `identical strings have ratio 1`() {
        val engine = SimilarityEngine()
        assertTrue(engine.similarityRatio("UBER", "UBER") >= 0.99f)
    }

    @Test
    fun `similar strings above threshold`() {
        val engine = SimilarityEngine()
        val ratio = engine.similarityRatio("MCDONALDS", "MCDONALD")
        assertTrue(ratio >= SimilarityEngine.SUGGEST_THRESHOLD)
    }

    @Test
    fun `different strings have low ratio`() {
        val engine = SimilarityEngine()
        val ratio = engine.similarityRatio("SPOTIFY", "CARREFOUR")
        assertTrue(ratio < SimilarityEngine.SUGGEST_THRESHOLD)
    }

    @Test
    fun `auto assigns when score is at or above default threshold`() {
        val engine = SimilarityEngine(autoAssignThreshold = SimilarityEngine.DEFAULT_THRESHOLD)
        val rules = listOf(rule("MCDONALDS", 1L))
        val result = engine.classify("MCDONALD", rules, emptyList())
        assertNotNull(result)
        assertEquals(ClassificationSource.SIMILARITY, result?.source)
        assertFalse(result!!.needsReview)
    }

    @Test
    fun `marks needsReview when score is between suggest and auto thresholds`() {
        val engine = SimilarityEngine(autoAssignThreshold = 0.95f)
        val rules = listOf(rule("MCDONALDS", 1L))
        val result = engine.classify("MCDONALD", rules, emptyList())
        assertNotNull(result)
        assertTrue(result!!.needsReview)
    }

    @Test
    fun `returns null when score is below suggest threshold`() {
        val engine = SimilarityEngine()
        val rules = listOf(rule("SPOTIFY", 6L))
        assertNull(engine.classify("CARREFOUR EXPRESS", rules, emptyList()))
    }

    private fun rule(pattern: String, categoryId: Long) = ClassificationRule(
        pattern = pattern,
        matchType = MatchType.CONTAINS,
        categoryId = categoryId,
        createdAt = now,
    )
}
