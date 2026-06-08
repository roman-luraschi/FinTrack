package com.fintrack.core.domain.usecase.ingestion

import com.fintrack.core.domain.common.DomainResult
import com.fintrack.core.domain.model.IngestionRequest
import com.fintrack.core.domain.model.IngestionResult
import com.fintrack.core.domain.model.TransactionDraft
import com.fintrack.core.domain.model.TransactionType
import com.fintrack.core.domain.repository.TransactionIngestionPort
import com.fintrack.core.domain.repository.UserSettingsPort
import com.fintrack.core.domain.usecase.classification.ClassifyExpenseUseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IngestTransactionsUseCase @Inject constructor(
    private val ingestionPort: TransactionIngestionPort,
    private val classifyExpenseUseCase: ClassifyExpenseUseCase,
    private val userSettingsPort: UserSettingsPort,
) {
    suspend operator fun invoke(request: IngestionRequest): DomainResult<IngestionResult> {
        if (request.operationId.isBlank()) {
            return DomainResult.Error("El operationId es obligatorio")
        }
        if (request.drafts.isEmpty()) {
            return DomainResult.Error("La ingesta debe incluir al menos un movimiento")
        }
        val fuzzyThreshold = userSettingsPort.observeFuzzyThreshold().first()
        val classifiedDrafts = request.drafts.map { draft ->
            classifyDraftIfNeeded(draft, fuzzyThreshold)
        }
        return DomainResult.Success(
            ingestionPort.ingest(request.copy(drafts = classifiedDrafts)),
        )
    }

    private suspend fun classifyDraftIfNeeded(
        draft: TransactionDraft,
        fuzzyThreshold: Float,
    ): TransactionDraft {
        if (draft.type != TransactionType.EXPENSE || draft.categoryId != null) {
            return draft
        }
        val result = classifyExpenseUseCase(draft.description, fuzzyThreshold)
        return draft.copy(
            categoryId = result.categoryId,
            subcategoryId = result.subcategoryId,
            classificationSource = result.source,
            classificationConfidence = result.confidence,
            classificationNeedsReview = result.needsReview,
        )
    }
}
