package com.fintrack.core.data.notification

import com.fintrack.core.data.dto.BankNotificationDto

object MarketingNotificationKeywords {
    private val keywords = listOf(
        "promo",
        "promoción",
        "promocion",
        "descuento",
        "encuesta",
        "sorteo",
        "ganá",
        "gana ",
        "beneficio",
        "newsletter",
        "invertí",
        "inverti",
        "inversión",
        "inversion",
        "dólar oficial",
        "dolar oficial",
        "rendir tu sueldo",
        "hacé rendir",
        "hace rendir",
    )

    fun isMarketing(dto: BankNotificationDto): Boolean {
        val content = listOfNotNull(dto.title, dto.text, dto.bigText, dto.subText)
            .joinToString(" ")
            .lowercase()
        return keywords.any { keyword -> content.contains(keyword) }
    }
}
