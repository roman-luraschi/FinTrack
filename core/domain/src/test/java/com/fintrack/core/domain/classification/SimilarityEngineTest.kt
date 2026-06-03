package com.fintrack.core.domain.classification

import org.junit.Assert.assertTrue
import org.junit.Test

class SimilarityEngineTest {
    private val engine = SimilarityEngine()

    @Test
    fun `identical strings have ratio 1`() {
        assertTrue(engine.similarityRatio("UBER", "UBER") >= 0.99f)
    }

    @Test
    fun `similar strings above threshold`() {
        val ratio = engine.similarityRatio("MCDONALDS", "MCDONALD")
        assertTrue(ratio >= SimilarityEngine.SUGGEST_THRESHOLD)
    }

    @Test
    fun `different strings have low ratio`() {
        val ratio = engine.similarityRatio("SPOTIFY", "CARREFOUR")
        assertTrue(ratio < SimilarityEngine.SUGGEST_THRESHOLD)
    }
}
