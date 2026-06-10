package com.fintrack.core.data.mercadopago

import com.fintrack.core.data.mapper.toDraft
import com.fintrack.core.data.network.dto.MercadoPagoMovementsResponseDto
import com.fintrack.core.domain.common.DomainResult
import com.fintrack.core.domain.model.TransactionSource
import com.fintrack.core.domain.model.TransactionType
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MercadoPagoMovementsMapperTest {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun `maps approved payments and skips rejected`() {
        val json = readFixture("mercadopago/payments_search.json")
        val response = moshi.adapter(MercadoPagoMovementsResponseDto::class.java).fromJson(json)!!

        val result = MercadoPagoMovementsMapper.map(response, moshi)

        assertEquals(2, result.transactions.size)
        assertEquals(1, result.skipped)
        assertTrue(result.parseErrors.isEmpty())

        val expense = result.transactions.first { it.id == "20359978" }
        assertEquals("Carrefour", expense.description)
        assertEquals("2350", expense.transactionAmount)
        assertEquals("approved", expense.status)
        assertEquals("test@example.com", expense.payerEmail)
        assertEquals("35.25", expense.feeAmount)
        assertEquals("2314.75", expense.netReceivedAmount)

        val income = result.transactions.first { it.id == "20359980" }
        assertEquals("Ingreso de Juan", income.description)
    }

    @Test
    fun `mapped dto converts to transaction draft`() {
        val json = readFixture("mercadopago/payments_search.json")
        val response = moshi.adapter(MercadoPagoMovementsResponseDto::class.java).fromJson(json)!!
        val mapped = MercadoPagoMovementsMapper.map(response, moshi)
        val dto = mapped.transactions.first { it.id == "20359978" }

        val draftResult = dto.toDraft(accountId = 7L)

        assertTrue(draftResult is DomainResult.Success)
        val draft = (draftResult as DomainResult.Success).data
        assertEquals("20359978", draft.externalId)
        assertEquals(7L, draft.accountId)
        assertEquals(TransactionSource.MERCADO_PAGO_API, draft.source)
        assertEquals(TransactionType.EXPENSE, draft.type)
    }

    private fun readFixture(path: String): String =
        javaClass.classLoader!!.getResource(path)!!.readText()
}
