package com.fintrack.app.feature.notification

import android.app.Notification
import android.service.notification.StatusBarNotification
import com.fintrack.core.data.dto.BankNotificationDto
import javax.inject.Inject

interface StatusBarNotificationMapper {
    fun toDto(sbn: StatusBarNotification): BankNotificationDto?
}

class DefaultStatusBarNotificationMapper @Inject constructor() : StatusBarNotificationMapper {

    override fun toDto(sbn: StatusBarNotification): BankNotificationDto? {
        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()

        if (!hasUsableContent(title, text, bigText)) return null

        return BankNotificationDto(
            packageName = sbn.packageName,
            notificationId = sbn.key,
            postedAt = sbn.postTime,
            title = title,
            text = text,
            bigText = bigText,
            subText = subText,
            channelId = sbn.notification.channelId,
        )
    }

    private fun hasUsableContent(title: String, text: String, bigText: String?): Boolean =
        title.isNotBlank() || text.isNotBlank() || !bigText.isNullOrBlank()
}
