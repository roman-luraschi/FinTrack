package com.fintrack.core.domain.usecase.classification

import com.fintrack.core.domain.classification.ExpenseClassifier
import com.fintrack.core.domain.classification.MerchantNormalizer
import com.fintrack.core.domain.common.DomainResult
import com.fintrack.core.domain.model.ChangeReason
import com.fintrack.core.domain.model.ClassificationResult
import com.fintrack.core.domain.model.ClassificationRule
import com.fintrack.core.domain.model.ClassificationSource
import com.fintrack.core.domain.model.LearnedMerchantCategory
import com.fintrack.core.domain.model.MatchType
import com.fintrack.core.domain.model.TransactionChange
import com.fintrack.core.domain.repository.CategoryRepository
import com.fintrack.core.domain.repository.ClassificationRepository
import com.fintrack.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClassifyExpenseUseCase @Inject constructor(
    private val classificationRepository: ClassificationRepository,
    private val categoryRepository: CategoryRepository,
    private val expenseClassifier: ExpenseClassifier,
) {
    suspend operator fun invoke(
        description: String,
        fuzzyThreshold: Float,
    ): ClassificationResult {
        val rules = classificationRepository.getActiveRules()
        val learned = classificationRepository.getLearnedMappings()
        val defaultCategoryId = resolveDefaultCategoryId()

        return expenseClassifier.classify(
            description = description,
            rules = rules,
            learnedMappings = learned,
            defaultCategoryId = defaultCategoryId,
            fuzzyThreshold = fuzzyThreshold,
        )
    }

    private suspend fun resolveDefaultCategoryId(): Long? {
        val roots = categoryRepository.observeRootCategories().first()
            .filter { it.deletedAt == null }
        return roots.find { it.name.equals(DEFAULT_CATEGORY_NAME, ignoreCase = true) }?.id
            ?: roots.filter { !it.isSystem }.maxByOrNull { it.sortOrder }?.id
            ?: roots.lastOrNull()?.id
    }

    companion object {
        private const val DEFAULT_CATEGORY_NAME = "Sin clasificar"
    }
}

@Singleton
class ObserveClassificationRulesUseCase @Inject constructor(
    private val classificationRepository: ClassificationRepository,
) {
    operator fun invoke(): Flow<List<ClassificationRule>> = classificationRepository.observeRules()
}

@Singleton
class AddClassificationRuleUseCase @Inject constructor(
    private val classificationRepository: ClassificationRepository,
) {
    suspend operator fun invoke(
        pattern: String,
        matchType: MatchType,
        categoryId: Long,
        priority: Int = 50,
    ): DomainResult<Long> {
        if (pattern.isBlank()) return DomainResult.Error("El patrón es obligatorio")
        val id = classificationRepository.insertRule(
            ClassificationRule(
                pattern = MerchantNormalizer.normalize(pattern),
                matchType = matchType,
                categoryId = categoryId,
                priority = priority,
                createdAt = Instant.now(),
            ),
        )
        return DomainResult.Success(id)
    }
}

@Singleton
class DeleteClassificationRuleUseCase @Inject constructor(
    private val classificationRepository: ClassificationRepository,
) {
    suspend operator fun invoke(id: Long): DomainResult<Unit> {
        classificationRepository.deleteRule(id)
        return DomainResult.Success(Unit)
    }
}

@Singleton
class ObserveLearnedMappingsUseCase @Inject constructor(
    private val classificationRepository: ClassificationRepository,
) {
    operator fun invoke(): Flow<List<LearnedMerchantCategory>> =
        classificationRepository.observeLearnedMappings()
}

@Singleton
class LearnFromCorrectionUseCase @Inject constructor(
    private val classificationRepository: ClassificationRepository,
) {
    suspend operator fun invoke(
        merchantNormalized: String,
        categoryId: Long,
        subcategoryId: Long? = null,
    ): DomainResult<Unit> {
        if (merchantNormalized.isBlank()) return DomainResult.Error("Comercio inválido")
        val now = Instant.now()
        classificationRepository.upsertLearnedMapping(
            LearnedMerchantCategory(
                merchantNormalized = merchantNormalized,
                categoryId = categoryId,
                subcategoryId = subcategoryId,
                learnedAt = now,
                updatedAt = now,
            ),
        )
        return DomainResult.Success(Unit)
    }
}

@Singleton
class RevertLearnedMappingUseCase @Inject constructor(
    private val classificationRepository: ClassificationRepository,
) {
    suspend operator fun invoke(id: Long): DomainResult<Unit> {
        classificationRepository.softDeleteLearnedMapping(id, Instant.now())
        return DomainResult.Success(Unit)
    }
}

@Singleton
class RecordCategoryCorrectionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val learnFromCorrectionUseCase: LearnFromCorrectionUseCase,
) {
    suspend operator fun invoke(
        transactionId: Long,
        newCategoryId: Long,
        subcategoryId: Long? = null,
    ): DomainResult<Unit> {
        val transaction = transactionRepository.getTransaction(transactionId)
            ?: return DomainResult.Error("Transacción no encontrada")

        val now = Instant.now()
        val updated = transaction.copy(
            categoryId = newCategoryId,
            subcategoryId = subcategoryId,
            classificationSource = ClassificationSource.USER_OVERRIDE,
            classificationConfidence = null,
            needsReview = false,
            updatedAt = now,
        )

        val changes = listOf(
            TransactionChange(
                transactionId = transactionId,
                fieldName = "categoryId",
                oldValue = transaction.categoryId?.toString(),
                newValue = newCategoryId.toString(),
                changedAt = now,
                changeReason = ChangeReason.RECLASSIFICATION,
            ),
        )

        transactionRepository.updateTransaction(updated, changes)
        learnFromCorrectionUseCase(transaction.merchantNormalized, newCategoryId, subcategoryId)
        return DomainResult.Success(Unit)
    }
}

@Singleton
class AcceptClassificationSuggestionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(transactionId: Long): DomainResult<Unit> {
        val transaction = transactionRepository.getTransaction(transactionId)
            ?: return DomainResult.Error("Transacción no encontrada")
        if (!transaction.needsReview) {
            return DomainResult.Success(Unit)
        }

        val now = Instant.now()
        val updated = transaction.copy(
            needsReview = false,
            updatedAt = now,
        )
        val changes = listOf(
            TransactionChange(
                transactionId = transactionId,
                fieldName = "needsReview",
                oldValue = "true",
                newValue = "false",
                changedAt = now,
                changeReason = ChangeReason.USER_EDIT,
            ),
        )
        transactionRepository.updateTransaction(updated, changes)
        return DomainResult.Success(Unit)
    }
}
