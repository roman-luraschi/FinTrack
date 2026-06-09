package com.fintrack.core.data.notification

import com.fintrack.core.data.dto.BankNotificationDto
import com.fintrack.core.domain.model.Account
import com.fintrack.core.domain.model.AccountType
import com.fintrack.core.domain.model.IntegrationProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class NotificationRelevanceFilterTest {

    private val now = Instant.parse("2026-06-02T16:38:00Z")

    private val ciudadAccount = Account(
        id = 1L,
        name = "Ciudad",
        type = AccountType.BANK,
        integrationProvider = IntegrationProvider.CIUDAD,
        notificationListenerEnabled = true,
        createdAt = now,
        updatedAt = now,
    )

    @Test
    fun `rejects package not in allowlist`() {
        val filter = filterWithAccounts(listOf(ciudadAccount))
        val dto = dto(
            packageName = "com.whatsapp",
            title = "Aviso de compra",
            text = "Compraste en MERPAGO por $ 4.900,00",
        )
        assertFalse(filter.isRelevant(dto))
    }

    @Test
    fun `accepts banco ciudad purchase notification when account matches`() {
        val filter = filterWithAccounts(listOf(ciudadAccount))
        val dto = dto(
            packageName = IntegrationProviderResolver.BANCO_CIUDAD_PACKAGE,
            title = "Aviso de compra",
            text = "Compraste en MERPAGO*LADESPENSADEL por $ 4.900,00 con tu Visa débito 6136.",
        )
        assertTrue(filter.isRelevant(dto))
    }

    @Test
    fun `accepts banco ciudad incoming transfer notification`() {
        val filter = filterWithAccounts(listOf(ciudadAccount))
        val dto = dto(
            packageName = IntegrationProviderResolver.BANCO_CIUDAD_PACKAGE,
            title = "¡Recibiste dinero!",
            text = "LURASCHI/ANDREA V te envió $ 30.000,00.",
        )
        assertTrue(filter.isRelevant(dto))
    }

    @Test
    fun `rejects banco ciudad when no matching enabled account`() {
        val filter = filterWithAccounts(emptyList())
        val dto = dto(
            packageName = IntegrationProviderResolver.BANCO_CIUDAD_PACKAGE,
            title = "Aviso de compra",
            text = "Compraste en MERPAGO*SUBEVIAJES por $ 894,17",
        )
        assertFalse(filter.isRelevant(dto))
    }

    @Test
    fun `rejects banco ciudad when account provider mismatches`() {
        val filter = filterWithAccounts(
            listOf(
                ciudadAccount.copy(integrationProvider = IntegrationProvider.MERCADO_PAGO),
            ),
        )
        val dto = dto(
            packageName = IntegrationProviderResolver.BANCO_CIUDAD_PACKAGE,
            title = "Aviso de compra",
            text = "Compraste en MERPAGO*SUBEVIAJES por $ 894,17",
        )
        assertFalse(filter.isRelevant(dto))
    }

    @Test
    fun `rejects banco ciudad marketing notification`() {
        val filter = filterWithAccounts(listOf(ciudadAccount))
        val dto = dto(
            packageName = IntegrationProviderResolver.BANCO_CIUDAD_PACKAGE,
            title = "Hacé rendir tu sueldo, comprá dólar oficial",
            text = "Invertí de forma fácil y segura",
        )
        assertFalse(filter.isRelevant(dto))
    }

    private fun filterWithAccounts(accounts: List<Account>): NotificationRelevanceFilter =
        NotificationRelevanceFilterImpl(
            enabledAccountsProvider = object : NotificationEnabledAccountsProvider {
                override fun snapshot(): List<Account> = accounts
            },
        )

    private fun dto(
        packageName: String,
        title: String,
        text: String,
    ): BankNotificationDto = BankNotificationDto(
        packageName = packageName,
        notificationId = "test-key",
        postedAt = now.toEpochMilli(),
        title = title,
        text = text,
    )
}
