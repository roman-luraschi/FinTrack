package com.fintrack.core.data.notification.parser

import com.fintrack.core.data.mapper.toDraft
import com.fintrack.core.domain.common.DomainResult
import com.fintrack.core.domain.model.IntegrationProvider
import com.fintrack.core.domain.model.TransactionSource
import com.fintrack.core.domain.model.ParseStatus
import com.fintrack.core.domain.model.TransactionStatus
import com.fintrack.core.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class NotificationParserTest {

    private val registry = NotificationParserRegistry(
        mercadoPagoNotificationParser = MercadoPagoNotificationParser(),
        genericBankNotificationParser = GenericBankNotificationParser(),
    )

    @Test
    fun `mercadopago payment fixture parses expense with merchant`() {
        val dto = NotificationFixtureLoader.load("mercadopago_payment.json")
        val result = registry.parse(dto)
        val parsed = assertSuccess(result)

        assertEquals(BigDecimal("2350.00"), parsed.amount)
        assertEquals(TransactionType.EXPENSE, parsed.type)
        assertEquals("Carrefour", parsed.description)
    }

    @Test
    fun `mercadopago income fixture parses income with payer`() {
        val dto = NotificationFixtureLoader.load("mercadopago_income.json")
        val result = registry.parse(dto)
        val parsed = assertSuccess(result)

        assertEquals(BigDecimal("500.00"), parsed.amount)
        assertEquals(TransactionType.INCOME, parsed.type)
        assertEquals("María García", parsed.description)
    }

    @Test
    fun `ciudad purchase fixture parses via generic parser`() {
        val dto = NotificationFixtureLoader.load("ciudad_purchase.json")
        val result = registry.parse(dto)
        val parsed = assertSuccess(result)

        assertEquals(BigDecimal("4900.00"), parsed.amount)
        assertEquals(TransactionType.EXPENSE, parsed.type)
        assertEquals("MERPAGO*LADESPENSADEL", parsed.description)
    }

    @Test
    fun `ciudad incoming transfer fixture parses income`() {
        val dto = NotificationFixtureLoader.load("ciudad_incoming_transfer.json")
        val result = registry.parse(dto)
        val parsed = assertSuccess(result)

        assertEquals(BigDecimal("30000.00"), parsed.amount)
        assertEquals(TransactionType.INCOME, parsed.type)
        assertEquals("LURASCHI/ANDREA V", parsed.description)
    }

    @Test
    fun `ciudad outgoing transfer fixture parses transfer`() {
        val dto = NotificationFixtureLoader.load("ciudad_outgoing_transfer.json")
        val result = registry.parse(dto)
        val parsed = assertSuccess(result)

        assertEquals(BigDecimal("15000.00"), parsed.amount)
        assertEquals(TransactionType.TRANSFER, parsed.type)
        assertEquals("Transferencia enviada", parsed.description)
    }

    @Test
    fun `registry routes mercadopago package to mercado pago parser`() {
        val parser = registry.parserFor("com.mercadopago.wallet")
        assertTrue(parser is MercadoPagoNotificationParser)
    }

    @Test
    fun `registry routes unknown bank package to generic parser`() {
        val parser = registry.parserFor("ar.com.redlink.custom")
        assertTrue(parser is GenericBankNotificationParser)
    }

    @Test
    fun `toDraft with registry produces valid transaction draft for ciudad purchase`() {
        val dto = NotificationFixtureLoader.load("ciudad_purchase.json")
        val result = dto.toDraft(accountId = 1L, parser = registry::parse)
        val draft = assertSuccess(result)

        assertEquals("ar.com.redlink.custom:ciudad-purchase-1", draft.externalId)
        assertEquals(BigDecimal("4900.00"), draft.amount)
        assertEquals("ARS", draft.currency)
        assertEquals(TransactionType.EXPENSE, draft.type)
        assertEquals(TransactionSource.BANK_NOTIFICATION, draft.source)
        assertEquals(TransactionStatus.CONFIRMED, draft.status)
        assertEquals(IntegrationProvider.CIUDAD, draft.provenance.integrationProvider)
    }

    @Test
    fun `ambiguous ciudad expense parses with needs review`() {
        val dto = NotificationFixtureLoader.load("ciudad_ambiguous_expense.json")
        val result = registry.parse(dto)
        val parsed = assertSuccess(result)

        assertEquals(BigDecimal("100.00"), parsed.amount)
        assertTrue(parsed.needsReview)
    }

    @Test
    fun `toDraft marks ambiguous notification as needs review`() {
        val dto = NotificationFixtureLoader.load("ciudad_ambiguous_expense.json")
        val result = dto.toDraft(accountId = 1L, parser = registry::parse)
        val draft = assertSuccess(result)

        assertEquals(TransactionStatus.NEEDS_REVIEW, draft.status)
        assertEquals(ParseStatus.PARTIAL, draft.provenance.parseStatus)
    }

    @Test
    fun `toDraft with registry produces valid transaction draft for mercadopago payment`() {
        val dto = NotificationFixtureLoader.load("mercadopago_payment.json")
        val result = dto.toDraft(accountId = 2L, parser = registry::parse)
        val draft = assertSuccess(result)

        assertEquals("com.mercadopago.wallet:mp-payment-1", draft.externalId)
        assertEquals(BigDecimal("2350.00"), draft.amount)
        assertEquals(IntegrationProvider.MERCADO_PAGO, draft.provenance.integrationProvider)
        assertEquals("Carrefour", draft.description)
    }

    private fun <T> assertSuccess(result: DomainResult<T>): T {
        assertTrue(result is DomainResult.Success)
        return (result as DomainResult.Success).data
    }
}
