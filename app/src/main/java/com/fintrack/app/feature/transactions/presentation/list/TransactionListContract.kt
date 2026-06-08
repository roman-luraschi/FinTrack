package com.fintrack.app.feature.transactions.presentation.list

import com.fintrack.core.domain.model.Account
import com.fintrack.core.domain.model.Category
import com.fintrack.core.domain.model.TransactionType

data class TransactionListItem(
    val id: Long,
    val description: String,
    val formattedAmount: String,
    val isExpense: Boolean,
    val categoryName: String?,
    val dateLabel: String,
    val needsReview: Boolean,
    val suggestedCategoryName: String?,
)

data class TransactionListUiState(
    val items: List<TransactionListItem> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val searchQuery: String = "",
    val selectedAccountId: Long? = null,
    val selectedCategoryId: Long? = null,
    val selectedType: TransactionType? = null,
    val selectedAccountName: String? = null,
    val selectedCategoryName: String? = null,
    val hasActiveFilters: Boolean = false,
    val isFilteredEmpty: Boolean = false,
    val isFilterSheetVisible: Boolean = false,
    val pendingDeleteId: Long? = null,
    val isDeleting: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

sealed interface TransactionListUserEvent {
    data class SearchQueryChanged(val query: String) : TransactionListUserEvent
    data class TypeFilterChanged(val type: TransactionType?) : TransactionListUserEvent
    data class AccountFilterChanged(val accountId: Long?) : TransactionListUserEvent
    data class CategoryFilterChanged(val categoryId: Long?) : TransactionListUserEvent
    data object ToggleFilterSheet : TransactionListUserEvent
    data object ClearFilters : TransactionListUserEvent
    data class MovementClicked(val id: Long) : TransactionListUserEvent
    data class DeleteRequested(val id: Long) : TransactionListUserEvent
    data class AcceptSuggestionRequested(val id: Long) : TransactionListUserEvent
    data object DeleteConfirmed : TransactionListUserEvent
    data object DeleteDismissed : TransactionListUserEvent
    data class ApplyNavArgs(val accountId: Long?, val categoryId: Long?) : TransactionListUserEvent
}

sealed interface TransactionListUiEffect {
    data class NavigateToEdit(val transactionId: Long) : TransactionListUiEffect
    data object MovementDeleted : TransactionListUiEffect
    data class ShowError(val message: String) : TransactionListUiEffect
    data object SuggestionAccepted : TransactionListUiEffect
}
