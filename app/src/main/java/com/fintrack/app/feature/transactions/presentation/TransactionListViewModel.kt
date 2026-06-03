package com.fintrack.app.feature.transactions.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.app.feature.accounts.domain.ObserveAccountsUseCase
import com.fintrack.app.feature.categories.domain.ObserveRootCategoriesUseCase
import com.fintrack.app.feature.transactions.domain.ObserveTransactionsUseCase
import com.fintrack.core.common.MoneyFormatter
import com.fintrack.core.domain.model.Account
import com.fintrack.core.domain.model.Category
import com.fintrack.core.domain.model.Transaction
import com.fintrack.core.domain.model.TransactionFilter
import com.fintrack.core.domain.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class TransactionListItem(
    val id: Long,
    val description: String,
    val formattedAmount: String,
    val isExpense: Boolean,
    val categoryName: String?,
    val dateLabel: String,
    val needsReview: Boolean,
)

data class TransactionListUiState(
    val items: List<TransactionListItem> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val searchQuery: String = "",
    val selectedAccountId: Long? = null,
    val selectedCategoryId: Long? = null,
    val selectedType: TransactionType? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    observeTransactionsUseCase: ObserveTransactionsUseCase,
    observeAccountsUseCase: ObserveAccountsUseCase,
    observeRootCategoriesUseCase: ObserveRootCategoriesUseCase,
) : ViewModel() {

    private val searchQuery = savedStateHandle.getStateFlow("search_query", "")
    private val selectedAccountId = savedStateHandle.getStateFlow<Long?>("account_id", null)
    private val selectedCategoryId = savedStateHandle.getStateFlow<Long?>("category_id", null)
    private val selectedType = savedStateHandle.getStateFlow<TransactionType?>("type", null)

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val filterFlow = combine(
        searchQuery,
        selectedAccountId,
        selectedCategoryId,
        selectedType,
    ) { search, accountId, categoryId, type ->
        TransactionFilter(
            accountId = accountId,
            categoryId = categoryId,
            type = type,
            searchQuery = search.takeIf { it.isNotBlank() },
        )
    }

    private val transactionsFlow = filterFlow.flatMapLatest { filter ->
        observeTransactionsUseCase(filter)
    }

    val uiState: StateFlow<TransactionListUiState> = combine(
        combine(
            transactionsFlow,
            observeAccountsUseCase(),
            observeRootCategoriesUseCase(),
        ) { transactions, accounts, categories ->
            Triple(transactions, accounts, categories)
        },
        combine(searchQuery, selectedAccountId, selectedCategoryId, selectedType) { search, accountId, categoryId, type ->
            FilterUi(search, accountId, categoryId, type)
        },
    ) { (transactions, accounts, categories), filterUi ->
        val categoryMap = categories.associateBy { it.id }
        TransactionListUiState(
            items = transactions.map { tx ->
                TransactionListItem(
                    id = tx.id,
                    description = tx.description,
                    formattedAmount = MoneyFormatter.format(tx.amount),
                    isExpense = tx.type == TransactionType.EXPENSE,
                    categoryName = tx.categoryId?.let { categoryMap[it]?.name },
                    dateLabel = com.fintrack.core.common.DateUtils.formatDate(tx.transactionDate),
                    needsReview = tx.needsReview,
                )
            },
            accounts = accounts,
            categories = categories,
            searchQuery = filterUi.search,
            selectedAccountId = filterUi.accountId,
            selectedCategoryId = filterUi.categoryId,
            selectedType = filterUi.type,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionListUiState(),
    )

    fun onSearchQueryChange(query: String) {
        savedStateHandle["search_query"] = query
    }

    fun onAccountFilterChange(accountId: Long?) {
        savedStateHandle["account_id"] = accountId
    }

    fun onCategoryFilterChange(categoryId: Long?) {
        savedStateHandle["category_id"] = categoryId
    }

    fun onTypeFilterChange(type: TransactionType?) {
        savedStateHandle["type"] = type
    }

    fun applyNavArgs(accountId: Long?, categoryId: Long?) {
        if (accountId != null) savedStateHandle["account_id"] = accountId
        if (categoryId != null) savedStateHandle["category_id"] = categoryId
    }
}

private data class FilterUi(
    val search: String,
    val accountId: Long?,
    val categoryId: Long?,
    val type: TransactionType?,
)
