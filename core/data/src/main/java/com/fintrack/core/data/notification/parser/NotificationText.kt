package com.fintrack.core.data.notification.parser

import com.fintrack.core.data.dto.BankNotificationDto

internal fun BankNotificationDto.combinedText(): String =
    listOfNotNull(title, text, bigText, subText)
        .filter { it.isNotBlank() }
        .joinToString(" ")
