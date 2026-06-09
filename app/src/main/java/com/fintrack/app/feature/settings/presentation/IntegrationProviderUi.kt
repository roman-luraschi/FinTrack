package com.fintrack.app.feature.settings.presentation

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.fintrack.R
import com.fintrack.core.domain.model.IntegrationProvider

val selectableIntegrationProviders = listOf(
    IntegrationProvider.MERCADO_PAGO,
    IntegrationProvider.CIUDAD,
    IntegrationProvider.GALICIA,
    IntegrationProvider.SANTANDER,
    IntegrationProvider.BBVA,
    IntegrationProvider.MACRO,
    IntegrationProvider.BRUBANK,
    IntegrationProvider.UALA,
    IntegrationProvider.PERSONAL_PAY,
    IntegrationProvider.GENERIC_BANK,
)

@StringRes
fun IntegrationProvider.labelRes(): Int = when (this) {
    IntegrationProvider.MERCADO_PAGO -> R.string.integration_provider_mercado_pago
    IntegrationProvider.CIUDAD -> R.string.integration_provider_ciudad
    IntegrationProvider.GALICIA -> R.string.integration_provider_galicia
    IntegrationProvider.SANTANDER -> R.string.integration_provider_santander
    IntegrationProvider.BBVA -> R.string.integration_provider_bbva
    IntegrationProvider.MACRO -> R.string.integration_provider_macro
    IntegrationProvider.BRUBANK -> R.string.integration_provider_brubank
    IntegrationProvider.UALA -> R.string.integration_provider_uala
    IntegrationProvider.PERSONAL_PAY -> R.string.integration_provider_personal_pay
    IntegrationProvider.GENERIC_BANK -> R.string.integration_provider_generic_bank
    IntegrationProvider.UNKNOWN -> R.string.account_integration_provider_none
}

@Composable
fun IntegrationProvider.displayLabel(): String = stringResource(labelRes())
