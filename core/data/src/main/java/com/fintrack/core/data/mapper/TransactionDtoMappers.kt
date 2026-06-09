package com.fintrack.core.data.mapper

import com.fintrack.core.domain.common.DomainResult
import com.fintrack.core.data.dto.BankNotificationDto
import com.fintrack.core.data.dto.CsvTransactionRowDto
import com.fintrack.core.data.dto.IngestionBatchDto
import com.fintrack.core.data.dto.IngestionItemDto
import com.fintrack.core.data.dto.ManualTransactionDto
import com.fintrack.core.data.dto.MercadoPagoTransactionDto
import com.fintrack.core.data.dto.OcrReceiptDto
import com.fintrack.core.data.notification.IntegrationProviderResolver
import com.fintrack.core.domain.classification.MerchantNormalizer
import com.fintrack.core.domain.model.IntegrationProvider
import com.fintrack.core.domain.model.ParseStatus
import com.fintrack.core.domain.model.IngestionRequest
import com.fintrack.core.domain.model.ProvenanceDraft
import com.fintrack.core.domain.model.TransactionDraft
import com.fintrack.core.domain.model.TransactionSource
import com.fintrack.core.domain.model.TransactionStatus
import com.fintrack.core.domain.model.TransactionType
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

const val PARSER_VERSION = "1.0.0"

data class ParsedNotificationFields(
    val amount: BigDecimal,
    val type: TransactionType,
    val description: String,
    val transactionDate: Instant,
    val needsReview: Boolean = false,
)

data class CsvColumnMapping(
    val dateColumn: String = "date",
    val amountColumn: String = "amount",
    val descriptionColumn: String = "description",
    val referenceColumn: String? = "reference",
    val typeColumn: String? = null,
    val currencyColumn: String? = "currency",
)

fun IngestionBatchDto.toDrafts(
    notificationParser: (BankNotificationDto) -> DomainResult<ParsedNotificationFields>,
    csvMapping: CsvColumnMapping = CsvColumnMapping(),
): DomainResult<List<TransactionDraft>> {
    val drafts = mutableListOf<TransactionDraft>()
    for (item in items) {
        val result = when (item) {
            is IngestionItemDto.Manual -> item.dto.toDraft()
            is IngestionItemDto.BankNotification -> {
                val accountId = targetAccountId
                    ?: return DomainResult.Error("targetAccountId requerido para notificaciones bancarias")
                item.dto.toDraft(accountId, notificationParser)
            }
            is IngestionItemDto.MercadoPago -> {
                val accountId = targetAccountId
                    ?: return DomainResult.Error("targetAccountId requerido para Mercado Pago")
                item.dto.toDraft(accountId)
            }
            is IngestionItemDto.OcrReceipt -> {
                val accountId = targetAccountId
                    ?: return DomainResult.Error("targetAccountId requerido para OCR")
                item.dto.toDraft(accountId)
            }
            is IngestionItemDto.CsvRow -> {
                val accountId = targetAccountId
                    ?: return DomainResult.Error("targetAccountId requerido para CSV")
                item.dto.toDraft(accountId, csvMapping)
            }
        }
        when (result) {
            is DomainResult.Success -> drafts.add(result.data)
            is DomainResult.Error -> return result
        }
    }
    return DomainResult.Success(drafts)
}

fun IngestionBatchDto.toIngestionRequest(
    notificationParser: (BankNotificationDto) -> DomainResult<ParsedNotificationFields>,
    csvMapping: CsvColumnMapping = CsvColumnMapping(),
): DomainResult<IngestionRequest> = when (val result = toDrafts(notificationParser, csvMapping)) {
    is DomainResult.Success -> DomainResult.Success(
        IngestionRequest(
            operationId = operationId,
            source = source,
            targetAccountId = targetAccountId,
            fileName = fileName,
            fileHash = fileHash,
            drafts = result.data,
        ),
    )
    is DomainResult.Error -> result
}

fun ManualTransactionDto.toDraft(): DomainResult<TransactionDraft> {
    val parsedAmount = parseAmount(amount)
        ?: return DomainResult.Error("Monto inválido: $amount")
    val parsedType = parseTransactionType(type)
        ?: return DomainResult.Error("Tipo de transacción inválido: $type")
    val parsedDate = parseInstant(transactionDate)
        ?: return DomainResult.Error("Fecha inválida: $transactionDate")
    if (description.isBlank()) return DomainResult.Error("La descripción es obligatoria")

    val capturedAt = Instant.now()
    val trimmedDescription = description.trim()

    return DomainResult.Success(
        TransactionDraft(
            externalId = null,
            amount = parsedAmount,
            currency = currency.uppercase(),
            type = parsedType,
            description = trimmedDescription,
            descriptionRaw = trimmedDescription,
            source = TransactionSource.MANUAL,
            accountId = accountId,
            transferAccountId = transferAccountId,
            transactionDate = parsedDate,
            status = TransactionStatus.CONFIRMED,
            provenance = ProvenanceDraft(
                integrationProvider = IntegrationProvider.UNKNOWN,
                rawPayload = """{"inputMethod":"form"}""",
                payloadFormat = "json",
                parseStatus = ParseStatus.SUCCESS,
                capturedAt = capturedAt,
            ),
        ),
    )
}

fun BankNotificationDto.toDraft(
    accountId: Long,
    parser: (BankNotificationDto) -> DomainResult<ParsedNotificationFields>,
): DomainResult<TransactionDraft> {
    val parsed = when (val result = parser(this)) {
        is DomainResult.Success -> result.data
        is DomainResult.Error -> return result
    }
    if (parsed.amount <= BigDecimal.ZERO) {
        return DomainResult.Error("Monto de notificación inválido")
    }

    val externalId = "$packageName:$notificationId"
    val provider = IntegrationProviderResolver.fromPackage(packageName)
    val rawPayload = buildNotificationPayload(this)
    val parseStatus = if (parsed.needsReview) ParseStatus.PARTIAL else ParseStatus.SUCCESS
    val status = if (parsed.needsReview) TransactionStatus.NEEDS_REVIEW else TransactionStatus.CONFIRMED
    val capturedAt = Instant.ofEpochMilli(postedAt)

    return DomainResult.Success(
        TransactionDraft(
            externalId = externalId,
            amount = parsed.amount.setScale(2, RoundingMode.HALF_UP),
            currency = "ARS",
            type = parsed.type,
            description = parsed.description.trim(),
            descriptionRaw = listOfNotNull(title, text, bigText).joinToString(" | "),
            source = TransactionSource.BANK_NOTIFICATION,
            accountId = accountId,
            transactionDate = parsed.transactionDate,
            status = status,
            provenance = ProvenanceDraft(
                integrationProvider = provider,
                providerCode = packageName,
                rawPayload = rawPayload,
                payloadFormat = "notification",
                parseStatus = parseStatus,
                capturedAt = capturedAt,
                metadataJson = buildNotificationMetadata(this),
            ),
        ),
    )
}

fun MercadoPagoTransactionDto.toDraft(accountId: Long): DomainResult<TransactionDraft> {
    val parsedAmount = parseAmount(transactionAmount)
        ?: return DomainResult.Error("Monto MP inválido: $transactionAmount")
    val parsedDate = parseInstant(dateCreated)
        ?: return DomainResult.Error("Fecha MP inválida: $dateCreated")
    if (description.isBlank()) return DomainResult.Error("Descripción MP vacía")

    val type = mapMercadoPagoOperationType(operationType)
    val status = when (status.lowercase()) {
        "approved" -> TransactionStatus.CONFIRMED
        "pending", "in_process" -> TransactionStatus.NEEDS_REVIEW
        else -> TransactionStatus.NEEDS_REVIEW
    }

    return DomainResult.Success(
        TransactionDraft(
            externalId = id,
            amount = parsedAmount.setScale(2, RoundingMode.HALF_UP),
            currency = currencyId.uppercase(),
            type = type,
            description = description.trim(),
            descriptionRaw = rawJson,
            source = TransactionSource.MERCADO_PAGO_API,
            accountId = accountId,
            transactionDate = parsedDate,
            status = status,
            provenance = ProvenanceDraft(
                integrationProvider = IntegrationProvider.MERCADO_PAGO,
                rawPayload = rawJson,
                payloadFormat = "json",
                parseStatus = ParseStatus.SUCCESS,
                capturedAt = parsedDate,
                metadataJson = buildMercadoPagoMetadata(this),
            ),
        ),
    )
}

fun OcrReceiptDto.toDraft(accountId: Long): DomainResult<TransactionDraft> {
    val capturedAt = Instant.now()
    val merchant = detectedMerchant?.trim().orEmpty()
    val description = merchant.ifBlank { "Ticket OCR" }

    val parsedAmount = detectedAmount?.let { parseAmount(it) }
    val parsedDate = detectedDate?.let { parseInstant(it) ?: parseLocalDate(it) }

    val isPartial = parsedAmount == null || parsedDate == null
    val amount = parsedAmount?.setScale(2, RoundingMode.HALF_UP) ?: BigDecimal.ZERO
    val transactionDate = parsedDate ?: capturedAt

    if (!isPartial && amount <= BigDecimal.ZERO) {
        return DomainResult.Error("Monto OCR inválido")
    }

    return DomainResult.Success(
        TransactionDraft(
            externalId = sessionId,
            amount = amount,
            currency = "ARS",
            type = TransactionType.EXPENSE,
            description = description,
            descriptionRaw = rawText,
            source = TransactionSource.OCR,
            accountId = accountId,
            transactionDate = transactionDate,
            status = if (isPartial) TransactionStatus.PENDING else TransactionStatus.CONFIRMED,
            provenance = ProvenanceDraft(
                integrationProvider = IntegrationProvider.UNKNOWN,
                rawPayload = rawText,
                payloadFormat = "ocr_text",
                parseStatus = if (isPartial) ParseStatus.PARTIAL else ParseStatus.SUCCESS,
                capturedAt = capturedAt,
                metadataJson = buildOcrMetadata(this),
            ),
        ),
    )
}

fun CsvTransactionRowDto.toDraft(
    accountId: Long,
    mapping: CsvColumnMapping,
): DomainResult<TransactionDraft> {
    val dateRaw = columns[mapping.dateColumn]
        ?: return DomainResult.Error("Fila ${rowNumber}: columna de fecha '${mapping.dateColumn}' ausente")
    val amountRaw = columns[mapping.amountColumn]
        ?: return DomainResult.Error("Fila ${rowNumber}: columna de monto '${mapping.amountColumn}' ausente")
    val descriptionRaw = columns[mapping.descriptionColumn]
        ?: return DomainResult.Error("Fila ${rowNumber}: columna de descripción '${mapping.descriptionColumn}' ausente")

    val parsedAmount = parseAmount(amountRaw)
        ?: return DomainResult.Error("Fila ${rowNumber}: monto inválido '$amountRaw'")
    val parsedDate = parseInstant(dateRaw) ?: parseLocalDate(dateRaw)
        ?: return DomainResult.Error("Fila ${rowNumber}: fecha inválida '$dateRaw'")
    if (parsedAmount <= BigDecimal.ZERO) {
        return DomainResult.Error("Fila ${rowNumber}: monto debe ser mayor a cero")
    }

    val type = mapping.typeColumn?.let { columns[it] }?.let { parseTransactionType(it) }
        ?: inferTypeFromAmount(amountRaw)

    val reference = mapping.referenceColumn?.let { columns[it] }
    val externalId = reference?.takeIf { it.isNotBlank() }
        ?: "$fileHash:$rowNumber"

    val currency = mapping.currencyColumn?.let { columns[it] }?.uppercase() ?: "ARS"

    return DomainResult.Success(
        TransactionDraft(
            externalId = externalId,
            amount = parsedAmount.abs().setScale(2, RoundingMode.HALF_UP),
            currency = currency,
            type = type,
            description = descriptionRaw.trim(),
            descriptionRaw = rawLine,
            source = TransactionSource.CSV_IMPORT,
            accountId = accountId,
            transactionDate = parsedDate,
            status = TransactionStatus.CONFIRMED,
            provenance = ProvenanceDraft(
                integrationProvider = IntegrationProvider.GENERIC_BANK,
                rawPayload = rawLine,
                payloadFormat = "csv_row",
                parseStatus = ParseStatus.SUCCESS,
                capturedAt = Instant.now(),
                metadataJson = """{"rowNumber":$rowNumber,"columnMappingId":"$columnMappingId"}""",
            ),
        ),
    )
}

fun TransactionDraft.merchantNormalized(): String = MerchantNormalizer.normalize(description)

fun TransactionDraft.toProvenance(transactionId: Long): com.fintrack.core.domain.model.TransactionProvenance =
    com.fintrack.core.domain.model.TransactionProvenance(
        transactionId = transactionId,
        integrationProvider = provenance.integrationProvider,
        providerCode = provenance.providerCode,
        rawPayload = provenance.rawPayload,
        payloadFormat = provenance.payloadFormat,
        parseStatus = provenance.parseStatus,
        parserVersion = PARSER_VERSION,
        capturedAt = provenance.capturedAt,
        metadataJson = provenance.metadataJson,
    )

private fun parseAmount(raw: String): BigDecimal? = runCatching {
    var trimmed = raw
        .trim()
        .replace("$", "")
        .replace("ARS", "", ignoreCase = true)
        .trim()
    val normalized = if (trimmed.contains(',')) {
        trimmed.replace(".", "").replace(",", ".")
    } else {
        trimmed.replace(Regex("[^0-9.-]"), "")
    }
    if (normalized.isBlank()) return null
    BigDecimal(normalized)
}.getOrNull()

private fun parseTransactionType(raw: String): TransactionType? = when (raw.uppercase()) {
    "EXPENSE", "GASTO", "DEBIT", "D" -> TransactionType.EXPENSE
    "INCOME", "INGRESO", "CREDIT", "C" -> TransactionType.INCOME
    "TRANSFER", "TRANSFERENCIA", "T" -> TransactionType.TRANSFER
    else -> null
}

private fun inferTypeFromAmount(amountRaw: String): TransactionType {
    val trimmed = amountRaw.trim()
    return if (trimmed.startsWith("-")) TransactionType.EXPENSE else TransactionType.EXPENSE
}

private fun parseInstant(raw: String): Instant? = runCatching {
    Instant.parse(raw.trim())
}.getOrNull()

private fun parseLocalDate(raw: String): Instant? {
    val trimmed = raw.trim()
    val formatters = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
    )
    for (formatter in formatters) {
        try {
            val date = LocalDate.parse(trimmed, formatter)
            return date.atStartOfDay(ZoneId.of("America/Argentina/Buenos_Aires")).toInstant()
        } catch (_: DateTimeParseException) {
            continue
        }
    }
    return null
}

private fun mapMercadoPagoOperationType(operationType: String): TransactionType = when {
    operationType.contains("money_transfer", ignoreCase = true) -> TransactionType.TRANSFER
    operationType.contains("payment", ignoreCase = true) -> TransactionType.EXPENSE
    operationType.contains("income", ignoreCase = true) -> TransactionType.INCOME
    else -> TransactionType.EXPENSE
}

private fun buildNotificationPayload(dto: BankNotificationDto): String =
    """{"packageName":"${escapeJson(dto.packageName)}","notificationId":"${escapeJson(dto.notificationId)}","title":"${escapeJson(dto.title)}","text":"${escapeJson(dto.text)}","bigText":"${escapeJson(dto.bigText.orEmpty())}","postedAt":${dto.postedAt}}"""

private fun buildNotificationMetadata(dto: BankNotificationDto): String =
    """{"packageName":"${escapeJson(dto.packageName)}","notificationId":"${escapeJson(dto.notificationId)}","channelId":"${escapeJson(dto.channelId.orEmpty())}","title":"${escapeJson(dto.title)}","postedAt":${dto.postedAt}}"""

private fun buildMercadoPagoMetadata(dto: MercadoPagoTransactionDto): String =
    """{"operationType":"${escapeJson(dto.operationType)}","paymentMethodId":"${escapeJson(dto.paymentMethodId.orEmpty())}","payerEmail":"${escapeJson(dto.payerEmail.orEmpty())}","feeAmount":"${escapeJson(dto.feeAmount.orEmpty())}","netReceivedAmount":"${escapeJson(dto.netReceivedAmount.orEmpty())}"}"""

private fun buildOcrMetadata(dto: OcrReceiptDto): String {
    val confidence = dto.confidenceOverall?.toString() ?: "null"
    val imageUri = escapeJson(dto.imageUri.orEmpty())
    return """{"imageUri":"$imageUri","ocrEngine":"${escapeJson(dto.ocrEngine)}","confidenceOverall":$confidence}"""
}

private fun escapeJson(value: String): String =
    value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
