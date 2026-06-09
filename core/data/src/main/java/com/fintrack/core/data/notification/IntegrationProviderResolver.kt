package com.fintrack.core.data.notification

import com.fintrack.core.domain.model.IntegrationProvider

object IntegrationProviderResolver {
    fun fromPackage(packageName: String): IntegrationProvider = when {
        packageName.contains("mercadopago", ignoreCase = true) -> IntegrationProvider.MERCADO_PAGO
        packageName.contains("galicia", ignoreCase = true) -> IntegrationProvider.GALICIA
        packageName.contains("santander", ignoreCase = true) -> IntegrationProvider.SANTANDER
        packageName.contains("bbva", ignoreCase = true) -> IntegrationProvider.BBVA
        packageName.contains("macro", ignoreCase = true) -> IntegrationProvider.MACRO
        packageName.contains("brubank", ignoreCase = true) -> IntegrationProvider.BRUBANK
        packageName.contains("uala", ignoreCase = true) -> IntegrationProvider.UALA
        packageName.contains("personalpay", ignoreCase = true) -> IntegrationProvider.PERSONAL_PAY
        packageName == BANCO_CIUDAD_PACKAGE || packageName.contains("buepp", ignoreCase = true) ->
            IntegrationProvider.CIUDAD
        else -> IntegrationProvider.GENERIC_BANK
    }

    /** Banca Móvil Ciudad (Google Play: ar.com.redlink.custom). */
    const val BANCO_CIUDAD_PACKAGE = "ar.com.redlink.custom"
}
