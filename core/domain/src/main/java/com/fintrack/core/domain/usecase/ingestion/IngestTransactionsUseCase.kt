package com.fintrack.core.domain.usecase.ingestion

import com.fintrack.core.domain.common.DomainResult
import com.fintrack.core.domain.model.IngestionRequest
import com.fintrack.core.domain.model.IngestionResult
import com.fintrack.core.domain.repository.TransactionIngestionPort
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IngestTransactionsUseCase @Inject constructor(
    private val ingestionPort: TransactionIngestionPort,
) {
    suspend operator fun invoke(request: IngestionRequest): DomainResult<IngestionResult> {
        if (request.operationId.isBlank()) {
            return DomainResult.Error("El operationId es obligatorio")
        }
        if (request.drafts.isEmpty()) {
            return DomainResult.Error("La ingesta debe incluir al menos un movimiento")
        }
        return DomainResult.Success(ingestionPort.ingest(request))
    }
}
