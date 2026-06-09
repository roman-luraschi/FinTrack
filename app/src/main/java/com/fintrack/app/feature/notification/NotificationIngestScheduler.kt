package com.fintrack.app.feature.notification

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.fintrack.core.data.dto.BankNotificationDto
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationIngestScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun enqueue(dto: BankNotificationDto) {
        val workName = uniqueWorkName(dto)
        val request = OneTimeWorkRequestBuilder<NotificationIngestWorker>()
            .setInputData(BankNotificationWorkerData.from(dto))
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            workName,
            ExistingWorkPolicy.KEEP,
            request,
        )
    }

    private fun uniqueWorkName(dto: BankNotificationDto): String {
        val sanitizedId = dto.notificationId
            .replace(Regex("[^a-zA-Z0-9._-]"), "_")
            .take(128)
        return "ingest-notification-${dto.packageName}-$sanitizedId"
    }
}
