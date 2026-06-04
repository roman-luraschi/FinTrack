package com.fintrack.core.data.dto

data class MercadoPagoTransactionDto(
    val id: String,
    val description: String,
    val transactionAmount: String,
    val currencyId: String,
    val dateCreated: String,
    val operationType: String,
    val status: String,
    val paymentMethodId: String? = null,
    val payerEmail: String? = null,
    val feeAmount: String? = null,
    val netReceivedAmount: String? = null,
    val rawJson: String,
)
