package com.fintrack.core.domain.classification

import org.junit.Assert.assertEquals
import org.junit.Test

class MerchantNormalizerTest {
    @Test
    fun `normalizes accents and case`() {
        assertEquals("LA FAROLA", MerchantNormalizer.normalize("La Farola"))
    }

    @Test
    fun `removes special characters`() {
        assertEquals("MCDONALDS 123", MerchantNormalizer.normalize("McDonald's #123"))
    }

    @Test
    fun `collapses whitespace`() {
        assertEquals("MERCADO LIBRE", MerchantNormalizer.normalize("  mercado   libre  "))
    }
}
