package com.fintrack.app.feature.mercadopago

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MercadoPagoSyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun schedulePeriodic() {
        val request = PeriodicWorkRequestBuilder<MercadoPagoSyncWorker>(SYNC_INTERVAL_HOURS, TimeUnit.HOURS)
            .setConstraints(networkConstraints())
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    fun scheduleImmediate() {
        val request = OneTimeWorkRequestBuilder<MercadoPagoSyncWorker>()
            .setConstraints(networkConstraints())
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    fun cancelAll() {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(PERIODIC_WORK_NAME)
        workManager.cancelUniqueWork(IMMEDIATE_WORK_NAME)
    }

    private fun networkConstraints(): Constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    private companion object {
        const val PERIODIC_WORK_NAME = "mercadopago-sync-periodic"
        const val IMMEDIATE_WORK_NAME = "mercadopago-sync-immediate"
        const val SYNC_INTERVAL_HOURS = 6L
    }
}
