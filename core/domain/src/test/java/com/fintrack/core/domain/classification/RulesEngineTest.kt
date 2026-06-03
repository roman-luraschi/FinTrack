package com.fintrack.core.domain.classification

import com.fintrack.core.domain.model.ClassificationRule
import com.fintrack.core.domain.model.MatchType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class RulesEngineTest {
    private val engine = RulesEngine()
    private val now = Instant.now()

    @Test
    fun `exact match returns category`() {
        val rules = listOf(
            rule("MCDONALDS", MatchType.CONTAINS, 1),
        )
        val result = engine.classify("COMPRA MCDONALDS PALERMO", rules)
        assertNotNull(result)
        assertEquals(1L, result!!.categoryId)
    }

    @Test
    fun `no match returns null`() {
        val rules = listOf(rule("SPOTIFY", MatchType.CONTAINS, 6))
        assertNull(engine.classify("FARMACITY", rules))
    }

    @Test
    fun `higher priority wins`() {
        val rules = listOf(
            rule("UBER", MatchType.CONTAINS, 3, priority = 10),
            rule("UBER EATS", MatchType.CONTAINS, 1, priority = 100),
        )
        val result = engine.classify("UBER EATS", rules)
        assertEquals(1L, result?.categoryId)
    }

    private fun rule(pattern: String, matchType: MatchType, categoryId: Long, priority: Int = 50) =
        ClassificationRule(
            pattern = pattern,
            matchType = matchType,
            categoryId = categoryId,
            priority = priority,
            createdAt = now,
        )
}
