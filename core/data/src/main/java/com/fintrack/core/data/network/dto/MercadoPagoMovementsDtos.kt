package com.fintrack.core.data.network.dto

import com.squareup.moshi.Json

data class MercadoPagoMovementsResponseDto(
    @Json(name = "results") val results: List<MercadoPagoPaymentDto> = emptyList(),
    @Json(name = "paging") val paging: MercadoPagoPagingDto? = null,
)

data class MercadoPagoPagingDto(
    @Json(name = "total") val total: Int? = null,
    @Json(name = "limit") val limit: Int? = null,
    @Json(name = "offset") val offset: Int? = null,
)

data class MercadoPagoPaymentDto(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "statement_descriptor") val statementDescriptor: String? = null,
    @Json(name = "reason") val reason: String? = null,
    @Json(name = "transaction_amount") val transactionAmount: Double? = null,
    @Json(name = "currency_id") val currencyId: String? = null,
    @Json(name = "date_created") val dateCreated: String? = null,
    @Json(name = "operation_type") val operationType: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "payment_method_id") val paymentMethodId: String? = null,
    @Json(name = "payer") val payer: MercadoPagoPayerDto? = null,
    @Json(name = "fee_details") val feeDetails: List<MercadoPagoFeeDetailDto>? = null,
    @Json(name = "transaction_details") val transactionDetails: MercadoPagoTransactionDetailsDto? = null,
)

data class MercadoPagoPayerDto(
    @Json(name = "email") val email: String? = null,
)

data class MercadoPagoFeeDetailDto(
    @Json(name = "amount") val amount: Double? = null,
)

data class MercadoPagoTransactionDetailsDto(
    @Json(name = "net_received_amount") val netReceivedAmount: Double? = null,
)
