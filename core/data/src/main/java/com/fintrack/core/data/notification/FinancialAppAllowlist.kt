package com.fintrack.core.data.notification

/**
 * Package names verificados en dispositivo real (jun 2026):
 * - Banco Ciudad: [ar.com.redlink.custom](https://play.google.com/store/apps/details?id=ar.com.redlink.custom)
 * - Mercado Pago: com.mercadopago.wallet (patrón mercadopago)
 * - Buepp (Ciudad): com.buepp
 *
 * Otros bancos usan heurística por substring hasta validar en dispositivo.
 */
object FinancialAppAllowlist {
    private val exactPackages = setOf(
        IntegrationProviderResolver.BANCO_CIUDAD_PACKAGE,
        "com.buepp",
        "com.mercadopago.wallet",
        "com.mercadolibre",
    )

    private val packageSubstrings = listOf(
        "mercadopago",
        "galicia",
        "santander",
        "bbva",
        "macro",
        "brubank",
        "uala",
        "personalpay",
        "bancociudad",
        "buepp",
    )

    fun isAllowed(packageName: String): Boolean {
        if (packageName in exactPackages) return true
        return packageSubstrings.any { substring -> packageName.contains(substring, ignoreCase = true) }
    }
}
