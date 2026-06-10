package com.fintrack.core.data.mercadopago

import com.fintrack.core.data.dto.MercadoPagoTransactionDto
import com.fintrack.core.data.network.dto.MercadoPagoMovementsResponseDto
import com.fintrack.core.data.network.dto.MercadoPagoPaymentDto
import com.squareup.moshi.Moshi

data class MercadoPagoMovementsMapResult(
    val transactions: List<MercadoPagoTransactionDto>,
    val skipped: Int,
    val parseErrors: List<String>,
)

object MercadoPagoMovementsMapper {

    private val INGESTIBLE_STATUSES = setOf("approved", "pending", "in_process")

    fun map(
        response: MercadoPagoMovementsResponseDto,
        moshi: Moshi,
    ): MercadoPagoMovementsMapResult {
        val paymentAdapter = moshi.adapter(MercadoPagoPaymentDto::class.java)
        val transactions = mutableListOf<MercadoPagoTransactionDto>()
        val parseErrors = mutableListOf<String>()
        var skipped = 0

        for (payment in response.results) {
            val status = payment.status?.lowercase()
            if (status == null || status !in INGESTIBLE_STATUSES) {
                skipped++
                continue
            }
            val rawJson = runCatching { paymentAdapter.toJson(payment) }.getOrDefault("{}")
            when (val mapped = payment.toTransactionDto(rawJson)) {
                null -> {
                    skipped++
                    parseErrors.add("Pago ${payment.id ?: "?"}: datos incompletos")
                }
                else -> transactions.add(mapped)
            }
        }

        return MercadoPagoMovementsMapResult(
            transactions = transactions,
            skipped = skipped,
            parseErrors = parseErrors,
        )
    }

    private fun MercadoPagoPaymentDto.toTransactionDto(rawJson: String): MercadoPagoTransactionDto? {
        val paymentId = id ?: return null
        val amount = transactionAmount ?: return null
        val created = dateCreated?.trim().takeUnless { it.isNullOrBlank() } ?: return null
        val currency = currencyId?.trim().takeUnless { it.isNullOrBlank() } ?: "ARS"
        val operation = operationType?.trim().takeUnless { it.isNullOrBlank() } ?: "regular_payment"
        val paymentStatus = status?.trim().takeUnless { it.isNullOrBlank() } ?: return null

        return MercadoPagoTransactionDto(
            id = paymentId.toString(),
            description = resolveDescription(),
            transactionAmount = formatAmount(amount),
            currencyId = currency,
            dateCreated = created,
            operationType = operation,
            status = paymentStatus,
            paymentMethodId = paymentMethodId,
            payerEmail = payer?.email,
            feeAmount = resolveFeeAmount(),
            netReceivedAmount = resolveNetReceivedAmount(),
            rawJson = rawJson,
        )
    }

    private fun MercadoPagoPaymentDto.resolveDescription(): String =
        listOfNotNull(description, statementDescriptor, reason)
            .firstOrNull { it.isNotBlank() }
            ?.trim()
            ?: "Movimiento Mercado Pago"

    private fun MercadoPagoPaymentDto.resolveFeeAmount(): String? {
        val totalFee = feeDetails
            ?.mapNotNull { it.amount }
            ?.takeIf { it.isNotEmpty() }
            ?.sum()
        return totalFee?.let(::formatAmount)
    }

    private fun MercadoPagoPaymentDto.resolveNetReceivedAmount(): String? =
        transactionDetails?.netReceivedAmount?.let(::formatAmount)

    private fun formatAmount(value: Double): String =
        if (value % 1.0 == 0.0) {
            value.toLong().toString()
        } else {
            value.toString()
        }
}
