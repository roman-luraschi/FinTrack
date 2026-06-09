package com.fintrack.core.data.notification.parser

import com.fintrack.core.data.dto.BankNotificationDto
import com.fintrack.core.data.mapper.ParsedNotificationFields
import com.fintrack.core.domain.common.DomainResult
import com.fintrack.core.domain.model.TransactionType
import java.time.Instant
import javax.inject.Inject

class MercadoPagoNotificationParser @Inject constructor() : NotificationParser {

    override fun supports(packageName: String): Boolean =
        packageName.contains("mercadopago", ignoreCase = true)

    override fun parse(dto: BankNotificationDto): DomainResult<ParsedNotificationFields> {
        val body = dto.combinedText()
        val amount = ArgentineAmountParser.findAmount(body)
            ?: return DomainResult.Error("No se detectó monto en notificación de Mercado Pago")

        val type = inferType(body)
        val description = extractDescription(dto.text, body, dto.title, type)

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
                lower.contains("cobraste") ||
                lower.contains("ingresó") ||
                lower.contains("ingreso") -> TransactionType.INCOME
            lower.contains("enviaste") ||
                lower.contains("transferiste") -> TransactionType.TRANSFER
            else -> TransactionType.EXPENSE
        }
    }

    private fun extractDescription(
        text: String,
        body: String,
        title: String,
        type: TransactionType,
    ): ParsedDescription {
        MERCHANT_PATTERN.find(text)?.groupValues?.get(1)?.trim()?.let { merchant ->
            if (merchant.isNotBlank()) return ParsedDescription(merchant, needsReview = false)
        }
        CONSUMPTION_PATTERN.find(text)?.groupValues?.get(1)?.trim()?.let { merchant ->
            if (merchant.isNotBlank()) return ParsedDescription(merchant, needsReview = false)
        }
        MERCHANT_PATTERN.find(body)?.groupValues?.get(1)?.trim()?.let { merchant ->
            if (merchant.isNotBlank()) return ParsedDescription(merchant, needsReview = false)
        }
        CONSUMPTION_PATTERN.find(body)?.groupValues?.get(1)?.trim()?.let { merchant ->
            if (merchant.isNotBlank()) return ParsedDescription(merchant, needsReview = false)
        }
        PAYER_PATTERN.find(text)?.groupValues?.get(1)?.trim()?.let { payer ->
            if (payer.isNotBlank()) return ParsedDescription(payer, needsReview = false)
        }
        return when (type) {
            TransactionType.INCOME -> ParsedDescription("Pago recibido Mercado Pago", needsReview = true)
            TransactionType.TRANSFER -> ParsedDescription("Transferencia Mercado Pago", needsReview = true)
            TransactionType.EXPENSE -> ParsedDescription(
                text = title.trim().ifBlank { "Pago Mercado Pago" },
                needsReview = true,
            )
        }
    }

    companion object {
        private val MERCHANT_PATTERN = Regex(
            """(?:en|a)\s+(.+?)\s+(?:con(?:sumiste)?|por|con)\s+(?:tu\s+)?(?:tarjeta|\$)""",
            RegexOption.IGNORE_CASE,
        )
        private val CONSUMPTION_PATTERN = Regex(
            """en\s+(.+?)\s+con\s+tu""",
            RegexOption.IGNORE_CASE,
        )
        private val PAYER_PATTERN = Regex(
            """(.+?)\s+te\s+envi[óo]""",
            RegexOption.IGNORE_CASE,
        )
    }
}
