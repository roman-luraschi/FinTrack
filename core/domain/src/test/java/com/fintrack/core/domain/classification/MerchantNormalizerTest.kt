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
        assertEquals("MCDONALD S 123", MerchantNormalizer.normalize("McDonald's #123"))
    }

    @Test
    fun `collapses whitespace`() {
        assertEquals("MERCADO LIBRE", MerchantNormalizer.normalize("  mercado   libre  "))
    }

    @Test
    fun `normalizes spanish accents in merchant names`() {
        assertEquals("PANADERIA EL NINO", MerchantNormalizer.normalize("Panadería El Niño"))
    }

    @Test
    fun `normalizes mercado pago style description with asterisk and numbers`() {
        assertEquals(
            "MCDONALD S 1234 PALERMO",
            MerchantNormalizer.normalize("*McDonald's #1234 - Palermo"),
        )
    }

    @Test
    fun `blank input returns empty string`() {
        assertEquals("", MerchantNormalizer.normalize("   "))
    }
}
