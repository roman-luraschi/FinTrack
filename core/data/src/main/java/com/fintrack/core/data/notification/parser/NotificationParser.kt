package com.fintrack.core.data.notification.parser

import com.fintrack.core.data.dto.BankNotificationDto
import com.fintrack.core.data.mapper.ParsedNotificationFields
import com.fintrack.core.domain.common.DomainResult

interface NotificationParser {
    fun supports(packageName: String): Boolean
    fun parse(dto: BankNotificationDto): DomainResult<ParsedNotificationFields>
}
