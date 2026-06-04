package com.fintrack.core.domain.repository

import com.fintrack.core.domain.model.IngestionRequest
import com.fintrack.core.domain.model.IngestionResult

interface TransactionIngestionPort {
    suspend fun ingest(request: IngestionRequest): IngestionResult
}
