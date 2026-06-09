package com.fintrack.core.data.notification.parser

import com.fintrack.core.data.dto.BankNotificationDto
import com.fintrack.core.data.mapper.ParsedNotificationFields
import com.fintrack.core.domain.common.DomainResult
import com.fintrack.core.domain.model.TransactionType
import java.time.Instant
import javax.inject.Inject

class GenericBankNotificationParser @Inject constructor() : NotificationParser {

    override fun supports(packageName: String): Boolean = true

    override fun parse(dto: BankNotificationDto): DomainResult<ParsedNotificationFields> {
        val body = dto.combinedText()
        val amount = ArgentineAmountParser.findAmount(body)
            ?: return DomainResult.Error("No se detectó monto en notificación bancaria")

        val type = inferType(body)
        val description = extractDescription(dto.text, dto.title, type)
            ?: ParsedDescription(
                text = dto.title.ifBlank { "Movimiento bancario" },
                needsReview = true,
            )

        return DomainResult.Success(
            ParsedNotificationFields(
                amount = amount,
                type = type,
                description = description.text,
                transactionDate = Instant.ofEpochMilli(dto.postedAt),
                needsReview = description.needsReview,
            ),
        )
    }

    private fun inferType(text: String): TransactionType {
        val lower = text.lowercase()
        return when {
            lower.contains("recibiste") ||
                lower.contains("te envió") ||
                lower.contains("te envio") ||
                lower.contains("ingresó") ||
                lower.contains("ingreso de") -> TransactionType.INCOME
            lower.contains("enviaste") ||
                lower.contains("transferiste") -> TransactionType.TRANSFER
            else -> TransactionType.EXPENSE
        }
    }

    private fun extractDescription(
        text: String,
        title: String,
        type: TransactionType,
    ): ParsedDescription? {
        PURCHASE_PATTERN.find(text)?.groupValues?.get(1)?.trim()?.let { merchant ->
            if (merchant.isNotBlank()) {
                return ParsedDescription(text = merchant, needsReview = false)
            }
        }
        INCOMING_TRANSFER_PATTERN.find(text)?.groupValues?.get(1)?.trim()?.let { sender ->
            if (sender.isNotBlank()) {
                return ParsedDescription(text = sender, needsReview = false)
            }
        }
        return when (type) {
            TransactionType.INCOME -> {
                val label = title.trim().ifBlank { "Ingreso bancario" }
                ParsedDescription(text = label, needsReview = title.isBlank())
            }
            TransactionType.TRANSFER -> ParsedDescription(
                text = "Transferencia enviada",
                needsReview = false,
            )
            TransactionType.EXPENSE -> title.trim().takeIf { it.isNotBlank() }?.let { expenseTitle ->
                ParsedDescription(text = expenseTitle, needsReview = true)
            }
        }
    }

    companion object {
        private val PURCHASE_PATTERN = Regex(
            """compraste en\s+(.+?)\s+por\s+\$""",
            RegexOption.IGNORE_CASE,
        )
        private val INCOMING_TRANSFER_PATTERN = Regex(
            """(.+?)\s+te\s+envi[óo]""",
            RegexOption.IGNORE_CASE,
        )
    }
}
