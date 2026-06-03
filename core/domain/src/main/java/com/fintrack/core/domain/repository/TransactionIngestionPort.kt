package com.fintrack.core.domain.repository

import com.fintrack.core.domain.model.IngestionBatch
import com.fintrack.core.domain.model.IngestionResult

interface TransactionIngestionPort {
    suspend fun ingest(batch: IngestionBatch): IngestionResult
}
