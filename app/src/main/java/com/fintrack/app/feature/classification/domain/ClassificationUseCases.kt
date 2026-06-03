package com.fintrack.app.feature.classification.domain

import com.fintrack.core.common.Result
import com.fintrack.core.domain.classification.ExpenseClassifier
import com.fintrack.core.domain.classification.MerchantNormalizer
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
        val defaultCategory = categoryRepository.getCategory(DEFAULT_CATEGORY_ID)
            ?: categoryRepository.observeRootCategories().first().lastOrNull()

        return expenseClassifier.classify(
            description = description,
            rules = rules,
            learnedMappings = learned,
            defaultCategoryId = defaultCategory?.id,
            fuzzyThreshold = fuzzyThreshold,
        )
    }

    companion object {
        const val DEFAULT_CATEGORY_ID = 14L
    }
}

class ObserveClassificationRulesUseCase @Inject constructor(
    private val classificationRepository: ClassificationRepository,
) {
    operator fun invoke(): Flow<List<ClassificationRule>> = classificationRepository.observeRules()
}

class AddClassificationRuleUseCase @Inject constructor(
    private val classificationRepository: ClassificationRepository,
) {
    suspend operator fun invoke(
        pattern: String,
        matchType: MatchType,
        categoryId: Long,
        priority: Int = 50,
    ): Result<Long> {
        if (pattern.isBlank()) return Result.Error("El patrón es obligatorio")
        val id = classificationRepository.insertRule(
            ClassificationRule(
                pattern = MerchantNormalizer.normalize(pattern),
                matchType = matchType,
                categoryId = categoryId,
                priority = priority,
                createdAt = Instant.now(),
            ),
        )
        return Result.Success(id)
    }
}

class DeleteClassificationRuleUseCase @Inject constructor(
    private val classificationRepository: ClassificationRepository,
) {
    suspend operator fun invoke(id: Long): Result<Unit> {
        classificationRepository.deleteRule(id)
        return Result.Success(Unit)
    }
}

class ObserveLearnedMappingsUseCase @Inject constructor(
    private val classificationRepository: ClassificationRepository,
) {
    operator fun invoke(): Flow<List<LearnedMerchantCategory>> =
        classificationRepository.observeLearnedMappings()
}

class LearnFromCorrectionUseCase @Inject constructor(
    private val classificationRepository: ClassificationRepository,
) {
    suspend operator fun invoke(
        merchantNormalized: String,
        categoryId: Long,
        subcategoryId: Long? = null,
    ): Result<Unit> {
        if (merchantNormalized.isBlank()) return Result.Error("Comercio inválido")
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
        return Result.Success(Unit)
    }
}

class RevertLearnedMappingUseCase @Inject constructor(
    private val classificationRepository: ClassificationRepository,
) {
    suspend operator fun invoke(id: Long): Result<Unit> {
        classificationRepository.softDeleteLearnedMapping(id, Instant.now())
        return Result.Success(Unit)
    }
}

class RecordCategoryCorrectionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val learnFromCorrectionUseCase: LearnFromCorrectionUseCase,
) {
    suspend operator fun invoke(
        transactionId: Long,
        newCategoryId: Long,
        subcategoryId: Long? = null,
    ): Result<Unit> {
        val transaction = transactionRepository.getTransaction(transactionId)
            ?: return Result.Error("Transacción no encontrada")

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
        return Result.Success(Unit)
    }
}
