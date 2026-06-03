package com.fintrack.core.domain.classification

object MerchantNormalizer {
    fun normalize(raw: String): String {
        if (raw.isBlank()) return ""

        val withoutAccents = java.text.Normalizer
            .normalize(raw, java.text.Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")

        return withoutAccents
            .uppercase()
            .replace("[^A-Z0-9\\s]".toRegex(), " ")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }
}
