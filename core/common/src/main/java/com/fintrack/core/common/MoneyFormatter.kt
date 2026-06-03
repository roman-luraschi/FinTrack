package com.fintrack.core.common

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

object MoneyFormatter {
    private val locale = Locale.forLanguageTag("es-AR")

    fun format(amount: BigDecimal, currencySymbol: String = "$"): String {
        val formatter = NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return "$currencySymbol ${formatter.format(amount)}"
    }

    fun parse(input: String): BigDecimal? {
        val cleaned = input
            .replace("$", "")
            .replace(" ", "")
            .replace(".", "")
            .replace(",", ".")
            .trim()
        return cleaned.toBigDecimalOrNull()?.setScale(2, java.math.RoundingMode.HALF_UP)
    }
}
