package com.fintrack.app.feature.notification

import androidx.work.Data
import com.fintrack.core.data.dto.BankNotificationDto

internal object BankNotificationWorkerData {
    const val KEY_PACKAGE_NAME = "package_name"
    const val KEY_NOTIFICATION_ID = "notification_id"
    const val KEY_POSTED_AT = "posted_at"
    const val KEY_TITLE = "title"
    const val KEY_TEXT = "text"
    const val KEY_BIG_TEXT = "big_text"
    const val KEY_SUB_TEXT = "sub_text"
    const val KEY_CHANNEL_ID = "channel_id"

    fun from(dto: BankNotificationDto): Data = Data.Builder()
        .putString(KEY_PACKAGE_NAME, dto.packageName)
        .putString(KEY_NOTIFICATION_ID, dto.notificationId)
        .putLong(KEY_POSTED_AT, dto.postedAt)
        .putString(KEY_TITLE, dto.title)
        .putString(KEY_TEXT, dto.text)
        .putString(KEY_BIG_TEXT, dto.bigText)
        .putString(KEY_SUB_TEXT, dto.subText)
        .putString(KEY_CHANNEL_ID, dto.channelId)
        .build()

    fun toDto(data: Data): BankNotificationDto? {
        val packageName = data.getString(KEY_PACKAGE_NAME) ?: return null
        val notificationId = data.getString(KEY_NOTIFICATION_ID) ?: return null
        val title = data.getString(KEY_TITLE) ?: return null
        val text = data.getString(KEY_TEXT) ?: return null
        return BankNotificationDto(
            packageName = packageName,
            notificationId = notificationId,
            postedAt = data.getLong(KEY_POSTED_AT, 0L),
            title = title,
            text = text,
            bigText = data.getString(KEY_BIG_TEXT),
            subText = data.getString(KEY_SUB_TEXT),
            channelId = data.getString(KEY_CHANNEL_ID),
        )
    }
}
