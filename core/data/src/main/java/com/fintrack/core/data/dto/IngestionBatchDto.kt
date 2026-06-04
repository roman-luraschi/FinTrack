package com.fintrack.core.data.dto

import com.fintrack.core.domain.model.TransactionSource

sealed class IngestionItemDto {
    data class Manual(val dto: ManualTransactionDto) : IngestionItemDto()
    data class BankNotification(val dto: BankNotificationDto) : IngestionItemDto()
    data class MercadoPago(val dto: MercadoPagoTransactionDto) : IngestionItemDto()
    data class OcrReceipt(val dto: OcrReceiptDto) : IngestionItemDto()
    data class CsvRow(val dto: CsvTransactionRowDto) : IngestionItemDto()
}

data class IngestionBatchDto(
    val operationId: String,
    val source: TransactionSource,
    val targetAccountId: Long?,
    val fileName: String?,
    val fileHash: String?,
    val items: List<IngestionItemDto>,
)
