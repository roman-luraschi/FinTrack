package com.fintrack.core.domain.usecase.transaction

import com.fintrack.core.domain.classification.MerchantNormalizer
import com.fintrack.core.domain.common.DomainResult
import com.fintrack.core.domain.model.ClassificationSource
import com.fintrack.core.domain.model.Transaction
import com.fintrack.core.domain.model.TransactionFilter
import com.fintrack.core.domain.model.TransactionSource
import com.fintrack.core.domain.model.TransactionStatus
import com.fintrack.core.domain.model.TransactionType
import com.fintrack.core.domain.repository.TransactionRepository
import com.fintrack.core.domain.repository.UserSettingsPort
import com.fintrack.core.domain.transaction.TransactionChangeRecorder
import com.fintrack.core.domain.transaction.TransactionValidator
import com.fintrack.core.domain.usecase.classification.ClassifyExpenseUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObserveTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    operator fun invoke(filter: TransactionFilter): Flow<List<Transaction>> =
        transactionRepository.observeTransactions(filter)
}

@Singleton
class ObserveTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    operator fun invoke(id: Long): Flow<Transaction?> = transactionRepository.observeTransaction(id)
}

@Singleton
class AddTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val classifyExpenseUseCase: ClassifyExpenseUseCase,
    private val userSettingsPort: UserSettingsPort,
) {
    suspend operator fun invoke(
        amount: BigDecimal,
        type: TransactionType,
        description: String,
        accountId: Long,
        transactionDate: Instant,
        notes: String? = null,
        categoryId: Long? = null,
    ): DomainResult<AddTransactionResult> {
        TransactionValidator.validateManualEntry(description, amount).let { validation ->
            if (validation is DomainResult.Error) return validation
        }

        val normalizedAmount = amount.setScale(2, RoundingMode.HALF_UP)
        val merchantNormalized = MerchantNormalizer.normalize(description)
        val now = Instant.now()
        val fuzzyThreshold = userSettingsPort.observeFuzzyThreshold().first()

        val classification = if (type == TransactionType.EXPENSE && categoryId == null) {
            classifyExpenseUseCase(description, fuzzyThreshold)
        } else {
            null
        }

        val resolvedCategoryId = categoryId ?: classification?.categoryId

        val duplicates = transactionRepository.findDuplicateCandidates(
            amount = normalizedAmount,
            merchantNormalized = merchantNormalized,
            transactionDate = transactionDate,
        )

        val transaction = Transaction(
            amount = normalizedAmount,
            currency = "ARS",
            type = type,
            status = TransactionStatus.CONFIRMED,
            description = description.trim(),
            merchantNormalized = merchantNormalized,
            categoryId = resolvedCategoryId,
            subcategoryId = classification?.subcategoryId,
            classificationSource = classification?.source ?: ClassificationSource.USER_OVERRIDE,
            classificationConfidence = classification?.confidence,
            needsReview = classification?.needsReview ?: false,
            source = TransactionSource.MANUAL,
            accountId = accountId,
            transactionDate = transactionDate,
            notes = notes?.trim()?.takeIf { it.isNotEmpty() },
            createdAt = now,
            updatedAt = now,
        )

        val id = transactionRepository.insertTransaction(transaction)
        return DomainResult.Success(
            AddTransactionResult(
                transactionId = id,
                duplicateCandidates = duplicates,
            ),
        )
    }
}

data class AddTransactionResult(
    val transactionId: Long,
    val duplicateCandidates: List<Transaction>,
)

@Singleton
class UpdateTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(
        transaction: Transaction,
        previous: Transaction,
    ): DomainResult<Unit> {
        val now = Instant.now()
        val changes = TransactionChangeRecorder.recordUserEdit(previous, transaction, now)

        transactionRepository.updateTransaction(
            transaction.copy(
                merchantNormalized = MerchantNormalizer.normalize(transaction.description),
                updatedAt = now,
            ),
            changes,
        )
        return DomainResult.Success(Unit)
    }
}

@Singleton
class DeleteTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(id: Long): DomainResult<Unit> {
        transactionRepository.softDeleteTransaction(id, Instant.now())
        return DomainResult.Success(Unit)
    }
}
