package com.fintrack.app.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.core.common.DateUtils
import com.fintrack.core.common.MoneyFormatter
import com.fintrack.core.domain.model.DashboardOverview
import com.fintrack.core.domain.model.DashboardPeriod
import com.fintrack.core.domain.model.TransactionType
import com.fintrack.core.domain.usecase.dashboard.ObserveDashboardOverviewUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class RecentMovementItem(
    val id: Long,
    val description: String,
    val formattedAmount: String,
    val isExpense: Boolean,
    val categoryName: String?,
    val dateLabel: String,
)

data class DashboardUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val formattedSpentToday: String = "",
    val formattedSpentThisMonth: String = "",
    val movementCount: Int = 0,
    val topCategoryName: String? = null,
    val topCategoryFormattedAmount: String? = null,
    val topCategoryId: Long? = null,
    val recentMovements: List<RecentMovementItem> = emptyList(),
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    observeDashboardOverviewUseCase: ObserveDashboardOverviewUseCase,
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = observeDashboardOverviewUseCase(DashboardPeriod.MONTH)
        .map { it.toUiState() }
        .catch { e ->
            emit(
                DashboardUiState(
                    isLoading = false,
                    errorMessage = e.message,
                ),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState(),
        )

    private fun DashboardOverview.toUiState(): DashboardUiState {
        val top = topExpenseCategory
        return DashboardUiState(
            isLoading = false,
            errorMessage = null,
            formattedSpentToday = MoneyFormatter.format(spentToday),
            formattedSpentThisMonth = MoneyFormatter.format(spentThisMonth),
            movementCount = movementCountInPeriod,
            topCategoryName = top?.categoryName,
            topCategoryFormattedAmount = top?.let { MoneyFormatter.format(it.total) },
            topCategoryId = top?.categoryId,
            recentMovements = recentTransactions.map { tx ->
                RecentMovementItem(
                    id = tx.id,
                    description = tx.description,
                    formattedAmount = MoneyFormatter.format(tx.amount),
                    isExpense = tx.type == TransactionType.EXPENSE,
                    categoryName = tx.categoryId?.let { categoryNamesById[it] },
                    dateLabel = DateUtils.formatDate(tx.transactionDate),
                )
            },
        )
    }
}
