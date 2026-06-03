package com.fintrack.app.feature.transactions.domain

import com.fintrack.core.common.Result
import com.fintrack.core.domain.classification.MerchantNormalizer
import com.fintrack.core.domain.model.ChangeReason
import com.fintrack.core.domain.model.Transaction
import com.fintrack.core.domain.model.TransactionChange
import com.fintrack.core.domain.model.TransactionFilter
import com.fintrack.core.domain.model.TransactionSource
import com.fintrack.core.domain.model.TransactionType
import com.fintrack.core.domain.repository.TransactionRepository
import com.fintrack.app.feature.classification.domain.ClassifyExpenseUseCase
import com.fintrack.app.data.preferences.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import javax.inject.Inject

class ObserveTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    operator fun invoke(filter: TransactionFilter): Flow<List<Transaction>> =
        transactionRepository.observeTransactions(filter)
}

class ObserveTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    operator fun invoke(id: Long): Flow<Transaction?> = transactionRepository.observeTransaction(id)
}

class AddTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val classifyExpenseUseCase: ClassifyExpenseUseCase,
    private val userPreferences: UserPreferences,
) {
    suspend operator fun invoke(
        amount: BigDecimal,
        type: TransactionType,
        description: String,
        accountId: Long,
        transactionDate: Instant,
        notes: String? = null,
        categoryId: Long? = null,
    ): Result<AddTransactionResult> {
        if (description.isBlank()) return Result.Error("La descripción es obligatoria")
        if (amount <= BigDecimal.ZERO) return Result.Error("El monto debe ser mayor a cero")

        val normalizedAmount = amount.setScale(2, RoundingMode.HALF_UP)
        val merchantNormalized = MerchantNormalizer.normalize(description)
        val now = Instant.now()
        val fuzzyThreshold = userPreferences.fuzzyThreshold.first()

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
            type = type,
            description = description.trim(),
            merchantNormalized = merchantNormalized,
            categoryId = resolvedCategoryId,
            subcategoryId = classification?.subcategoryId,
            classificationSource = classification?.source
                ?: com.fintrack.core.domain.model.ClassificationSource.USER_OVERRIDE,
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
        return Result.Success(
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

class UpdateTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(
        transaction: Transaction,
        previous: Transaction,
    ): Result<Unit> {
        val now = Instant.now()
        val changes = buildList {
            if (previous.amount != transaction.amount) {
                add(fieldChange(transaction.id, "amount", previous.amount.toPlainString(), transaction.amount.toPlainString(), now))
            }
            if (previous.description != transaction.description) {
                add(fieldChange(transaction.id, "description", previous.description, transaction.description, now))
            }
            if (previous.categoryId != transaction.categoryId) {
                add(fieldChange(transaction.id, "categoryId", previous.categoryId?.toString(), transaction.categoryId?.toString(), now))
            }
            if (previous.accountId != transaction.accountId) {
                add(fieldChange(transaction.id, "accountId", previous.accountId.toString(), transaction.accountId.toString(), now))
            }
            if (previous.transactionDate != transaction.transactionDate) {
                add(fieldChange(transaction.id, "transactionDate", previous.transactionDate.toString(), transaction.transactionDate.toString(), now))
            }
        }

        transactionRepository.updateTransaction(
            transaction.copy(
                merchantNormalized = MerchantNormalizer.normalize(transaction.description),
                updatedAt = now,
            ),
            changes,
        )
        return Result.Success(Unit)
    }

    private fun fieldChange(
        transactionId: Long,
        field: String,
        old: String?,
        new: String?,
        now: Instant,
    ) = TransactionChange(
        transactionId = transactionId,
        fieldName = field,
        oldValue = old,
        newValue = new,
        changedAt = now,
        changeReason = ChangeReason.USER_EDIT,
    )
}

class DeleteTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(id: Long): Result<Unit> {
        transactionRepository.softDeleteTransaction(id, Instant.now())
        return Result.Success(Unit)
    }
}
