package com.fintrack.core.data.dto

data class BankNotificationDto(
    val packageName: String,
    val notificationId: String,
    val postedAt: Long,
    val title: String,
    val text: String,
    val bigText: String? = null,
    val subText: String? = null,
    val channelId: String? = null,
)
