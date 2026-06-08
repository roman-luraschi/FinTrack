package com.fintrack.core.domain.classification

import com.fintrack.core.domain.model.ClassificationSource
import com.fintrack.core.domain.model.LearnedMerchantCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class LearningEngineTest {
    private val engine = LearningEngine()
    private val now = Instant.now()

    @Test
    fun `exact merchant match returns learned category`() {
        val learned = listOf(
            mapping("LA FAROLA", categoryId = 1L),
        )
        val result = engine.classify("LA FAROLA", learned)
        assertEquals(1L, result?.categoryId)
        assertEquals(ClassificationSource.LEARNED, result?.source)
        assertEquals(1.0f, result?.confidence)
    }

    @Test
    fun `soft deleted mapping is ignored`() {
        val learned = listOf(
            mapping("LA FAROLA", categoryId = 1L, deletedAt = now),
        )
        assertNull(engine.classify("LA FAROLA", learned))
    }

    @Test
    fun `returns subcategory when present`() {
        val learned = listOf(
            mapping("CARREFOUR", categoryId = 2L, subcategoryId = 21L),
        )
        val result = engine.classify("CARREFOUR", learned)
        assertEquals(2L, result?.categoryId)
        assertEquals(21L, result?.subcategoryId)
    }

    private fun mapping(
        merchant: String,
        categoryId: Long,
        subcategoryId: Long? = null,
        deletedAt: Instant? = null,
    ) = LearnedMerchantCategory(
        merchantNormalized = merchant,
        categoryId = categoryId,
        subcategoryId = subcategoryId,
        learnedAt = now,
        updatedAt = now,
        deletedAt = deletedAt,
    )
}
