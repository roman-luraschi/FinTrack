package com.fintrack.core.domain.usecase.mercadopago

import com.fintrack.core.domain.common.DomainResult
import com.fintrack.core.domain.model.Account
import com.fintrack.core.domain.model.IngestionRequest
import com.fintrack.core.domain.model.IntegrationProvider
import com.fintrack.core.domain.model.MercadoPagoConnectionStatus
import com.fintrack.core.domain.model.MercadoPagoSyncResult
import com.fintrack.core.domain.model.TransactionSource
import com.fintrack.core.domain.repository.AccountRepository
import com.fintrack.core.domain.repository.DeviceIdentityPort
import com.fintrack.core.domain.repository.MercadoPagoConnectionPort
import com.fintrack.core.domain.repository.MercadoPagoSyncMetadataPort
import com.fintrack.core.domain.repository.MercadoPagoSyncPort
import com.fintrack.core.domain.usecase.ingestion.IngestTransactionsUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncMercadoPagoMovementsUseCase @Inject constructor(
    private val deviceIdentityPort: DeviceIdentityPort,
    private val mercadoPagoConnectionPort: MercadoPagoConnectionPort,
    private val accountRepository: AccountRepository,
    private val mercadoPagoSyncPort: MercadoPagoSyncPort,
    private val syncMetadataPort: MercadoPagoSyncMetadataPort,
    private val ingestTransactionsUseCase: IngestTransactionsUseCase,
) {
    private val syncMutex = Mutex()

    suspend operator fun invoke(
        since: Instant? = null,
        limit: Int = 50,
    ): DomainResult<MercadoPagoSyncResult> = syncMutex.withLock {
        syncInternal(since = since, limit = limit)
    }

    private suspend fun syncInternal(
        since: Instant?,
        limit: Int,
    ): DomainResult<MercadoPagoSyncResult> {
        val effectiveSince = since ?: syncMetadataPort.getLastSyncAt()
        val connection = mercadoPagoConnectionPort.observeConnection().first()
        if (connection.status != MercadoPagoConnectionStatus.CONNECTED) {
            return DomainResult.Error("Mercado Pago no está conectado")
        }

        val account = resolveTargetAccount()
            ?: return DomainResult.Error(
                "Creá una cuenta y asociala a Mercado Pago en Ajustes",
            )

        val deviceId = deviceIdentityPort.getOrCreateDeviceId()
        val fetchResult = when (
            val result = mercadoPagoSyncPort.fetchMovementDrafts(
                deviceId = deviceId,
                accountId = account.id,
                since = effectiveSince,
                limit = limit,
            )
        ) {
            is DomainResult.Error -> return result
            is DomainResult.Success -> result.data
        }

        if (fetchResult.drafts.isEmpty()) {
            syncMetadataPort.setLastSyncAt(Instant.now())
            return DomainResult.Success(
                MercadoPagoSyncResult(
                    fetched = 0,
                    skipped = fetchResult.skipped,
                    inserted = 0,
                    updated = 0,
                    ingestionSkipped = 0,
                    errors = 0,
                    parseErrors = fetchResult.parseErrors,
                ),
            )
        }

        val operationId = buildOperationId(deviceId)
        return when (
            val ingestResult = ingestTransactionsUseCase(
                IngestionRequest(
                    operationId = operationId,
                    source = TransactionSource.MERCADO_PAGO_API,
                    targetAccountId = account.id,
                    drafts = fetchResult.drafts,
                ),
            )
        ) {
            is DomainResult.Error -> ingestResult
            is DomainResult.Success -> {
                syncMetadataPort.setLastSyncAt(Instant.now())
                DomainResult.Success(
                    MercadoPagoSyncResult(
                        fetched = fetchResult.drafts.size,
                        skipped = fetchResult.skipped,
                        inserted = ingestResult.data.inserted,
                        updated = ingestResult.data.updated,
                        ingestionSkipped = ingestResult.data.skipped,
                        errors = ingestResult.data.errors,
                        parseErrors = fetchResult.parseErrors,
                    ),
                )
            }
        }
    }

    private suspend fun resolveTargetAccount(): Account? {
        val accounts = accountRepository.observeAccounts().first()
        return accounts.firstOrNull { it.integrationProvider == IntegrationProvider.MERCADO_PAGO }
            ?: accountRepository.getDefaultAccount()
            ?: accounts.firstOrNull()
    }

    private fun buildOperationId(deviceId: String): String =
        "mp-sync:$deviceId:${Instant.now().toEpochMilli()}"
}
