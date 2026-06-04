package com.fintrack.core.data.repository

import com.fintrack.core.common.DispatcherProvider
import com.fintrack.core.data.ingestion.DedupResolution
import com.fintrack.core.data.ingestion.TransactionDedupService
import com.fintrack.core.database.dao.IngestionBatchDao
import com.fintrack.core.database.dao.TransactionDao
import com.fintrack.core.database.mapper.toEntity
import com.fintrack.core.domain.model.IngestionBatch
import com.fintrack.core.domain.model.IngestionBatchStatus
import com.fintrack.core.domain.model.IngestionRequest
import com.fintrack.core.domain.model.IngestionResult
import com.fintrack.core.domain.repository.TransactionIngestionPort
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionIngestionPortImpl @Inject constructor(
    private val ingestionBatchDao: IngestionBatchDao,
    private val transactionDao: TransactionDao,
    private val dedupService: TransactionDedupService,
    private val dispatchers: DispatcherProvider,
) : TransactionIngestionPort {

    override suspend fun ingest(request: IngestionRequest): IngestionResult = withContext(dispatchers.io) {
        val existingBatch = ingestionBatchDao.getByOperationId(request.operationId)
        if (existingBatch != null) {
            return@withContext IngestionResult(
                batchId = existingBatch.id,
                inserted = existingBatch.insertedCount,
                updated = existingBatch.updatedCount,
                skipped = existingBatch.skippedCount,
                errors = existingBatch.errorCount,
                duplicateCandidates = transactionDao.findDuplicateCandidateIdsByBatch(existingBatch.id),
            )
        }

        val now = Instant.now()
        val runningBatch = IngestionBatch(
            operationId = request.operationId,
            source = request.source,
            status = IngestionBatchStatus.RUNNING,
            targetAccountId = request.targetAccountId,
            fileName = request.fileName,
            fileHash = request.fileHash,
            recordCount = request.drafts.size,
            startedAt = now,
            createdAt = now,
        )
        val batchId = ingestionBatchDao.insert(runningBatch.toEntity())

        var inserted = 0
        var updated = 0
        var skipped = 0
        var errors = 0
        val duplicateCandidates = mutableListOf<Long>()
        val errorMessages = mutableListOf<String>()

        for (draft in request.drafts) {
            try {
                when (val resolution = dedupService.resolve(draft, batchId, now)) {
                    is DedupResolution.Skip -> skipped++

                    is DedupResolution.Update -> {
                        transactionDao.updateWithProvenance(
                            resolution.transaction.toEntity(),
                            resolution.provenance.toEntity(),
                        )
                        updated++
                    }

                    is DedupResolution.InsertDuplicateCandidate -> {
                        val id = transactionDao.insertWithProvenance(
                            resolution.transaction.toEntity(),
                            resolution.provenance.toEntity(),
                        )
                        duplicateCandidates.add(id)
                        inserted++
                    }

                    is DedupResolution.InsertNew -> {
                        transactionDao.insertWithProvenance(
                            resolution.transaction.toEntity(),
                            resolution.provenance.toEntity(),
                        )
                        inserted++
                    }
                }
            } catch (error: Exception) {
                errors++
                errorMessages.add(error.message ?: error.javaClass.simpleName)
            }
        }

        val completedAt = Instant.now()
        val finalStatus = when {
            errors == 0 -> IngestionBatchStatus.COMPLETED
            inserted + updated + skipped > 0 -> IngestionBatchStatus.PARTIAL
            else -> IngestionBatchStatus.FAILED
        }

        ingestionBatchDao.update(
            runningBatch.copy(
                id = batchId,
                status = finalStatus,
                insertedCount = inserted,
                updatedCount = updated,
                skippedCount = skipped,
                errorCount = errors,
                errorSummary = errorMessages.takeIf { it.isNotEmpty() }?.joinToString("; "),
                completedAt = completedAt,
            ).toEntity(),
        )

        IngestionResult(
            batchId = batchId,
            inserted = inserted,
            updated = updated,
            skipped = skipped,
            errors = errors,
            duplicateCandidates = duplicateCandidates,
        )
    }
}
