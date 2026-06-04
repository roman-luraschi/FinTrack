package com.fintrack.app.data.repository

import com.fintrack.core.domain.model.IngestionRequest
import com.fintrack.core.domain.model.IngestionResult
import com.fintrack.core.domain.repository.TransactionIngestionPort
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionIngestionPortStub @Inject constructor() : TransactionIngestionPort {
    override suspend fun ingest(request: IngestionRequest): IngestionResult {
        throw UnsupportedOperationException("Transaction ingestion is not implemented yet")
    }
}
