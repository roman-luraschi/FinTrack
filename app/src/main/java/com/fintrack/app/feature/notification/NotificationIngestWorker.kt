package com.fintrack.app.feature.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fintrack.core.data.mapper.toDraft
import com.fintrack.core.data.notification.IntegrationProviderResolver
import com.fintrack.core.data.notification.NotificationEnabledAccountsProvider
import com.fintrack.core.data.notification.parser.NotificationParserRegistry
import com.fintrack.core.domain.common.DomainResult
import com.fintrack.core.domain.model.IngestionRequest
import com.fintrack.core.domain.model.TransactionSource
import com.fintrack.core.domain.model.TransactionStatus
import com.fintrack.core.domain.usecase.ingestion.IngestTransactionsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class NotificationIngestWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val ingestTransactionsUseCase: IngestTransactionsUseCase,
    private val parserRegistry: NotificationParserRegistry,
    private val enabledAccountsProvider: NotificationEnabledAccountsProvider,
    private val movementDetectedNotifier: MovementDetectedNotifier,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val dto = BankNotificationWorkerData.toDto(inputData) ?: return Result.failure()

        val provider = IntegrationProviderResolver.fromPackage(dto.packageName)
        val account = enabledAccountsProvider.snapshot()
            .firstOrNull { it.integrationProvider == provider }
            ?: return Result.success()

        val draftResult = dto.toDraft(account.id, parserRegistry::parse)
        val draft = when (draftResult) {
            is DomainResult.Success -> draftResult.data
            is DomainResult.Error -> return Result.failure()
        }

        val request = IngestionRequest(
            operationId = "notif:${dto.packageName}:${dto.notificationId}:${dto.postedAt}",
            source = TransactionSource.BANK_NOTIFICATION,
            targetAccountId = account.id,
            drafts = listOf(draft),
        )

        return when (val ingestResult = ingestTransactionsUseCase(request)) {
            is DomainResult.Success -> {
                if (ingestResult.data.inserted > 0) {
                    movementDetectedNotifier.notifyIfEnabled(
                        amount = draft.amount,
                        description = draft.description,
                        needsReview = draft.status == TransactionStatus.NEEDS_REVIEW,
                    )
                }
                Result.success()
            }
            is DomainResult.Error -> Result.failure()
        }
    }
}
