package com.fintrack.core.data.ingestion

import com.fintrack.core.data.mapper.buildWeakDedupKey
import com.fintrack.core.data.mapper.toTransactionWithProvenance
import com.fintrack.core.database.dao.TransactionDao
import com.fintrack.core.database.mapper.toDomain
import com.fintrack.core.domain.model.DedupMatchType
import com.fintrack.core.domain.model.Transaction
import com.fintrack.core.domain.model.TransactionDraft
import com.fintrack.core.domain.model.TransactionProvenance
import com.fintrack.core.domain.model.TransactionStatus
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

sealed class DedupResolution {
    data object Skip : DedupResolution()

    data class Update(
        val existing: Transaction,
        val transaction: Transaction,
        val provenance: TransactionProvenance,
    ) : DedupResolution()

    data class InsertDuplicateCandidate(
        val transaction: Transaction,
        val provenance: TransactionProvenance,
        val matchedTransactionId: Long,
    ) : DedupResolution()

    data class InsertNew(
        val transaction: Transaction,
        val provenance: TransactionProvenance,
    ) : DedupResolution()
}

@Singleton
class TransactionDedupService @Inject constructor(
    private val transactionDao: TransactionDao,
) {
    suspend fun resolve(
        draft: TransactionDraft,
        ingestionBatchId: Long,
        now: Instant,
        windowMinutes: Long = 2,
    ): DedupResolution {
        val externalId = draft.externalId?.trim()?.takeIf { it.isNotEmpty() }
        if (externalId != null) {
            val existingEntity = transactionDao.findByExternalIdAndSource(externalId, draft.source)
            if (existingEntity != null) {
                val existing = existingEntity.toDomain()
                if (existing.status == TransactionStatus.CONFIRMED) {
                    return DedupResolution.Skip
                }
                val (transaction, provenance) = draft.toTransactionWithProvenance(
                    ingestionBatchId = ingestionBatchId,
                    now = now,
                    dedupMatchType = DedupMatchType.STRONG,
                    dedupMatchedTransactionId = existing.id,
                )
                return DedupResolution.Update(
                    existing = existing,
                    transaction = transaction.copy(id = existing.id, createdAt = existing.createdAt),
                    provenance = provenance.copy(transactionId = existing.id),
                )
            }
        }

        val merchantNormalized = com.fintrack.core.domain.classification.MerchantNormalizer.normalize(draft.description)
        val start = draft.transactionDate.minus(windowMinutes, ChronoUnit.MINUTES)
        val end = draft.transactionDate.plus(windowMinutes, ChronoUnit.MINUTES)
        val weakMatches = transactionDao.findDuplicatesInWindow(
            amount = draft.amount,
            merchantNormalized = merchantNormalized,
            startWindow = start,
            endWindow = end,
        )

        if (weakMatches.isNotEmpty()) {
            val matched = weakMatches.first().toDomain()
            val weakKey = buildWeakDedupKey(draft.amount, merchantNormalized, draft.transactionDate)
            val (transaction, provenance) = draft.toTransactionWithProvenance(
                ingestionBatchId = ingestionBatchId,
                now = now,
                dedupMatchType = DedupMatchType.WEAK,
                dedupMatchedTransactionId = matched.id,
                weakDedupKey = weakKey,
                statusOverride = TransactionStatus.DUPLICATE_CANDIDATE,
                needsReviewOverride = true,
            )
            return DedupResolution.InsertDuplicateCandidate(
                transaction = transaction,
                provenance = provenance,
                matchedTransactionId = matched.id,
            )
        }

        val (transaction, provenance) = draft.toTransactionWithProvenance(
            ingestionBatchId = ingestionBatchId,
            now = now,
        )
        return DedupResolution.InsertNew(transaction, provenance)
    }
}
