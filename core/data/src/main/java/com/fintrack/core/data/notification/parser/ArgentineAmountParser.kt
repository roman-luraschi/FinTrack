package com.fintrack.core.data.notification.parser

import java.math.BigDecimal

internal object ArgentineAmountParser {
    private val amountPattern = Regex(
        """(?:ARS\s*)?\$?\s*(\d{1,3}(?:\.\d{3})*,\d{2}|\d+(?:,\d{2})?)(?:\s*ARS)?""",
        RegexOption.IGNORE_CASE,
    )

    fun findAmount(text: String): BigDecimal? =
        amountPattern.find(text)?.groupValues?.get(1)?.let(::parseAmount)

    fun parseAmount(raw: String): BigDecimal? = runCatching {
        var trimmed = raw
            .trim()
            .replace("$", "")
            .replace("ARS", "", ignoreCase = true)
            .trim()
        val normalized = if (trimmed.contains(',')) {
            trimmed.replace(".", "").replace(",", ".")
        } else {
            trimmed.replace(Regex("[^0-9.-]"), "")
        }
        if (normalized.isBlank()) return null
        BigDecimal(normalized)
    }.getOrNull()
}
