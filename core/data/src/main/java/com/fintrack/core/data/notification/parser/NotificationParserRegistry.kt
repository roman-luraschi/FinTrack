package com.fintrack.core.data.notification.parser

import com.fintrack.core.data.dto.BankNotificationDto
import com.fintrack.core.data.mapper.ParsedNotificationFields
import com.fintrack.core.domain.common.DomainResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationParserRegistry @Inject constructor(
    mercadoPagoNotificationParser: MercadoPagoNotificationParser,
    private val genericBankNotificationParser: GenericBankNotificationParser,
) {
    private val specificParsers: List<NotificationParser> = listOf(
        mercadoPagoNotificationParser,
    )

    fun parserFor(packageName: String): NotificationParser =
        specificParsers.firstOrNull { it.supports(packageName) } ?: genericBankNotificationParser

    fun parse(dto: BankNotificationDto): DomainResult<ParsedNotificationFields> =
        parserFor(dto.packageName).parse(dto)
}
