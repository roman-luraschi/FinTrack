package com.fintrack.core.domain.repository

import com.fintrack.core.domain.common.DomainResult
import com.fintrack.core.domain.model.TransactionDraft
import java.time.Instant

data class MercadoPagoFetchResult(
    val drafts: List<TransactionDraft>,
    val skipped: Int,
    val parseErrors: List<String>,
)

interface MercadoPagoSyncPort {
    suspend fun fetchMovementDrafts(
        deviceId: String,
        accountId: Long,
        since: Instant? = null,
        limit: Int = 50,
    ): DomainResult<MercadoPagoFetchResult>
}
