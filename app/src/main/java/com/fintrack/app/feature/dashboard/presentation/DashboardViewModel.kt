package com.fintrack.app.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.app.data.preferences.UserPreferences
import com.fintrack.app.feature.dashboard.domain.ObserveDashboardSummaryUseCase
import com.fintrack.core.common.MoneyFormatter
import com.fintrack.core.domain.model.DashboardPeriod
import com.fintrack.core.domain.model.DashboardSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val summary: DashboardSummary? = null,
    val period: DashboardPeriod = DashboardPeriod.MONTH,
    val formattedExpenses: String = "",
    val formattedIncome: String = "",
    val formattedBalance: String = "",
    val errorMessage: String? = null,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    observeDashboardSummaryUseCase: ObserveDashboardSummaryUseCase,
    private val userPreferences: UserPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferences.dashboardPeriod.collect { period ->
                _uiState.update { it.copy(period = period) }
            }
        }
        viewModelScope.launch {
            observeDashboardSummaryUseCase()
                .catch { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
                .collect { summary ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            summary = summary,
                            formattedExpenses = MoneyFormatter.format(summary.totalExpenses),
                            formattedIncome = MoneyFormatter.format(summary.totalIncome),
                            formattedBalance = MoneyFormatter.format(summary.netBalance),
                            errorMessage = null,
                        )
                    }
                }
        }
    }

    fun setPeriod(period: DashboardPeriod) {
        viewModelScope.launch {
            userPreferences.setDashboardPeriod(period)
        }
    }
}
