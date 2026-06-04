package com.fintrack.core.data.mapper

import com.fintrack.core.domain.classification.MerchantNormalizer
import com.fintrack.core.domain.model.ClassificationSource
import com.fintrack.core.domain.model.DedupMatchType
import com.fintrack.core.domain.model.Transaction
import com.fintrack.core.domain.model.TransactionDraft
import com.fintrack.core.domain.model.TransactionProvenance
import com.fintrack.core.domain.model.TransactionStatus
import java.time.Instant

fun TransactionDraft.toTransactionWithProvenance(
    ingestionBatchId: Long,
    now: Instant,
    dedupMatchType: DedupMatchType = DedupMatchType.NONE,
    dedupMatchedTransactionId: Long? = null,
    weakDedupKey: String? = null,
    statusOverride: TransactionStatus? = null,
    needsReviewOverride: Boolean? = null,
): Pair<Transaction, TransactionProvenance> {
    val merchantNormalized = MerchantNormalizer.normalize(description)
    val resolvedStatus = statusOverride ?: status
    val resolvedNeedsReview = needsReviewOverride ?: (resolvedStatus == TransactionStatus.DUPLICATE_CANDIDATE)

    val transaction = Transaction(
        externalId = externalId,
        amount = amount,
        currency = currency,
        type = type,
        status = resolvedStatus,
        description = description.trim(),
        descriptionRaw = descriptionRaw,
        merchantNormalized = merchantNormalized,
        classificationSource = ClassificationSource.DEFAULT,
        needsReview = resolvedNeedsReview,
        source = source,
        accountId = accountId,
        transferAccountId = transferAccountId,
        transactionDate = transactionDate,
        ingestionBatchId = ingestionBatchId,
        createdAt = now,
        updatedAt = now,
    )

    val provenance = TransactionProvenance(
        transactionId = 0,
        integrationProvider = provenance.integrationProvider,
        providerCode = provenance.providerCode,
        rawPayload = provenance.rawPayload,
        payloadFormat = provenance.payloadFormat,
        parseStatus = provenance.parseStatus,
        parserVersion = PARSER_VERSION,
        dedupMatchType = dedupMatchType,
        dedupMatchedTransactionId = dedupMatchedTransactionId,
        weakDedupKey = weakDedupKey,
        capturedAt = provenance.capturedAt,
        metadataJson = provenance.metadataJson,
    )

    return transaction to provenance
}

fun buildWeakDedupKey(
    amount: java.math.BigDecimal,
    merchantNormalized: String,
    transactionDate: Instant,
): String = "${amount.toPlainString()}|$merchantNormalized|${transactionDate.toEpochMilli()}"
