package com.fintrack.core.domain.model

import java.time.Instant

data class IngestionBatch(
    val id: Long = 0,
    val operationId: String,
    val source: TransactionSource,
    val status: IngestionBatchStatus,
    val targetAccountId: Long? = null,
    val fileName: String? = null,
    val fileHash: String? = null,
    val recordCount: Int = 0,
    val insertedCount: Int = 0,
    val updatedCount: Int = 0,
    val skippedCount: Int = 0,
    val errorCount: Int = 0,
    val errorSummary: String? = null,
    val startedAt: Instant,
    val completedAt: Instant? = null,
    val createdAt: Instant,
)

data class IngestionRequest(
    val operationId: String,
    val source: TransactionSource,
    val targetAccountId: Long? = null,
    val fileName: String? = null,
    val fileHash: String? = null,
    val drafts: List<TransactionDraft>,
)

data class IngestionResult(
    val batchId: Long,
    val inserted: Int,
    val updated: Int,
    val skipped: Int,
    val errors: Int,
    val duplicateCandidates: List<Long>,
)
