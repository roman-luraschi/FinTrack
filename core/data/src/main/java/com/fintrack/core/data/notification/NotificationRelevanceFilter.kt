package com.fintrack.core.data.notification

import com.fintrack.core.data.dto.BankNotificationDto

interface NotificationRelevanceFilter {
    fun isRelevant(dto: BankNotificationDto): Boolean
}
