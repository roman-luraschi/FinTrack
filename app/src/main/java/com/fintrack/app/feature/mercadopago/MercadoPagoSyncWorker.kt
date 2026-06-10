package com.fintrack.app.feature.mercadopago

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fintrack.core.domain.common.DomainResult
import com.fintrack.core.domain.usecase.mercadopago.SyncMercadoPagoMovementsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class MercadoPagoSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncMercadoPagoMovementsUseCase: SyncMercadoPagoMovementsUseCase,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result =
        when (val result = syncMercadoPagoMovementsUseCase()) {
            is DomainResult.Success -> Result.success()
            is DomainResult.Error -> if (result.message.shouldSkipRetry()) {
                Result.success()
            } else {
                Result.retry()
            }
        }

    private fun String.shouldSkipRetry(): Boolean = contains("no está conectado", ignoreCase = true) ||
        contains("asociala a Mercado Pago", ignoreCase = true)
}
