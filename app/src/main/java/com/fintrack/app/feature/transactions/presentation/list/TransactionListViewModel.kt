package com.fintrack.app.feature.transactions.presentation.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.core.common.DateUtils
import com.fintrack.core.common.MoneyFormatter
import com.fintrack.core.domain.common.DomainResult
import com.fintrack.core.domain.model.TransactionFilter
import com.fintrack.core.domain.model.TransactionType
import com.fintrack.core.domain.usecase.account.ObserveAccountsUseCase
import com.fintrack.core.domain.usecase.category.ObserveRootCategoriesUseCase
import com.fintrack.core.domain.usecase.transaction.DeleteTransactionUseCase
import com.fintrack.core.domain.usecase.transaction.ObserveTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    observeTransactionsUseCase: ObserveTransactionsUseCase,
    observeAccountsUseCase: ObserveAccountsUseCase,
    observeRootCategoriesUseCase: ObserveRootCategoriesUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
) : ViewModel() {

    private val searchQuery = savedStateHandle.getStateFlow("search_query", "")
    private val selectedAccountId = savedStateHandle.getStateFlow<Long?>("account_id", null)
    private val selectedCategoryId = savedStateHandle.getStateFlow<Long?>("category_id", null)
    private val selectedType = savedStateHandle.getStateFlow<TransactionType?>("type", null)

    private val isFilterSheetVisible = MutableStateFlow(false)
    private val pendingDeleteId = MutableStateFlow<Long?>(null)
    private val isDeleting = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    private val effectsChannel = Channel<TransactionListUiEffect>(Channel.BUFFERED)
    val uiEffect = effectsChannel.receiveAsFlow()

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
        combine(isFilterSheetVisible, pendingDeleteId, isDeleting, errorMessage) { sheet, deleteId, deleting, error ->
            OverlayUi(sheet, deleteId, deleting, error)
        },
    ) { (transactions, accounts, categories), filterUi, overlay ->
        val categoryMap = categories.associateBy { it.id }
        val accountMap = accounts.associateBy { it.id }
        val hasActiveFilters = filterUi.search.isNotBlank() ||
            filterUi.accountId != null ||
            filterUi.categoryId != null ||
            filterUi.type != null
        val items = transactions.map { tx ->
            TransactionListItem(
                id = tx.id,
                description = tx.description,
                formattedAmount = MoneyFormatter.format(tx.amount),
                isExpense = tx.type == TransactionType.EXPENSE,
                categoryName = tx.categoryId?.let { categoryMap[it]?.name },
                dateLabel = DateUtils.formatDate(tx.transactionDate),
                needsReview = tx.needsReview,
            )
        }
        TransactionListUiState(
            items = items,
            accounts = accounts,
            categories = categories,
            searchQuery = filterUi.search,
            selectedAccountId = filterUi.accountId,
            selectedCategoryId = filterUi.categoryId,
            selectedType = filterUi.type,
            selectedAccountName = filterUi.accountId?.let { accountMap[it]?.name },
            selectedCategoryName = filterUi.categoryId?.let { categoryMap[it]?.name },
            hasActiveFilters = hasActiveFilters,
            isFilteredEmpty = hasActiveFilters && items.isEmpty(),
            isFilterSheetVisible = overlay.isFilterSheetVisible,
            pendingDeleteId = overlay.pendingDeleteId,
            isDeleting = overlay.isDeleting,
            isLoading = false,
            errorMessage = overlay.errorMessage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionListUiState(),
    )

    fun onEvent(event: TransactionListUserEvent) {
        when (event) {
            is TransactionListUserEvent.SearchQueryChanged ->
                savedStateHandle["search_query"] = event.query
            is TransactionListUserEvent.TypeFilterChanged ->
                savedStateHandle["type"] = event.type
            is TransactionListUserEvent.AccountFilterChanged ->
                savedStateHandle["account_id"] = event.accountId
            is TransactionListUserEvent.CategoryFilterChanged ->
                savedStateHandle["category_id"] = event.categoryId
            TransactionListUserEvent.ToggleFilterSheet ->
                isFilterSheetVisible.update { !it }
            TransactionListUserEvent.ClearFilters -> {
                savedStateHandle["search_query"] = ""
                savedStateHandle["account_id"] = null
                savedStateHandle["category_id"] = null
                savedStateHandle["type"] = null
            }
            is TransactionListUserEvent.MovementClicked ->
                viewModelScope.launch {
                    effectsChannel.send(TransactionListUiEffect.NavigateToEdit(event.id))
                }
            is TransactionListUserEvent.DeleteRequested ->
                pendingDeleteId.value = event.id
            TransactionListUserEvent.DeleteDismissed ->
                pendingDeleteId.value = null
            TransactionListUserEvent.DeleteConfirmed -> confirmDelete()
            is TransactionListUserEvent.ApplyNavArgs -> {
                if (event.accountId != null) savedStateHandle["account_id"] = event.accountId
                if (event.categoryId != null) savedStateHandle["category_id"] = event.categoryId
            }
        }
    }

    private fun confirmDelete() {
        val id = pendingDeleteId.value ?: return
        viewModelScope.launch {
            isDeleting.value = true
            errorMessage.value = null
            when (val result = deleteTransactionUseCase(id)) {
                is DomainResult.Success -> {
                    pendingDeleteId.value = null
                    effectsChannel.send(TransactionListUiEffect.MovementDeleted)
                }
                is DomainResult.Error -> {
                    errorMessage.value = result.message
                    effectsChannel.send(TransactionListUiEffect.ShowError(result.message))
                }
            }
            isDeleting.value = false
        }
    }
}

private data class FilterUi(
    val search: String,
    val accountId: Long?,
    val categoryId: Long?,
    val type: TransactionType?,
)

private data class OverlayUi(
    val isFilterSheetVisible: Boolean,
    val pendingDeleteId: Long?,
    val isDeleting: Boolean,
    val errorMessage: String?,
)
