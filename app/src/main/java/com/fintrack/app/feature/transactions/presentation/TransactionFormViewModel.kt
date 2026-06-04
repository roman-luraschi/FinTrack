package com.fintrack.app.feature.transactions.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.core.domain.usecase.account.GetDefaultAccountUseCase
import com.fintrack.core.domain.usecase.account.ObserveAccountsUseCase
import com.fintrack.core.domain.usecase.category.ObserveRootCategoriesUseCase
import com.fintrack.core.domain.usecase.transaction.AddTransactionUseCase
import com.fintrack.core.domain.usecase.transaction.DeleteTransactionUseCase
import com.fintrack.core.domain.usecase.transaction.ObserveTransactionUseCase
import com.fintrack.core.domain.usecase.transaction.UpdateTransactionUseCase
import com.fintrack.core.common.DateUtils
import com.fintrack.core.common.MoneyFormatter
import com.fintrack.core.domain.common.DomainResult
import com.fintrack.core.domain.model.Account
import com.fintrack.core.domain.model.Category
import com.fintrack.core.domain.model.Transaction
import com.fintrack.core.domain.model.TransactionType
import com.fintrack.core.navigation.NavArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

data class TransactionFormUiState(
    val transactionId: Long? = null,
    val amountInput: String = "",
    val description: String = "",
    val notes: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val selectedAccountId: Long? = null,
    val selectedCategoryId: Long? = null,
    val transactionDate: LocalDate = DateUtils.instantToLocalDate(Instant.now()),
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isSaving: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val duplicateWarning: String? = null,
    val savedSuccessfully: Boolean = false,
)

@HiltViewModel
class TransactionFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val observeTransactionUseCase: ObserveTransactionUseCase,
    private val observeAccountsUseCase: ObserveAccountsUseCase,
    private val observeRootCategoriesUseCase: ObserveRootCategoriesUseCase,
    private val getDefaultAccountUseCase: GetDefaultAccountUseCase,
) : ViewModel() {

    private val transactionId: Long? = savedStateHandle.get<Long>(NavArgs.TRANSACTION_ID)
    private val preselectedAccountId: Long? =
        savedStateHandle.get<Long>(NavArgs.ACCOUNT_ID)?.takeIf { it != NavArgs.NONE }

    private val _uiState = MutableStateFlow(TransactionFormUiState(transactionId = transactionId))
    val uiState: StateFlow<TransactionFormUiState> = _uiState.asStateFlow()

    private var loadedTransaction: Transaction? = null

    init {
        viewModelScope.launch {
            observeAccountsUseCase().collect { accounts ->
                _uiState.update { state ->
                    state.copy(
                        accounts = accounts,
                        selectedAccountId = state.selectedAccountId
                            ?: preselectedAccountId
                            ?: accounts.firstOrNull { it.isDefault }?.id
                            ?: accounts.firstOrNull()?.id,
                    )
                }
            }
        }
        viewModelScope.launch {
            observeRootCategoriesUseCase().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
        transactionId?.let { id ->
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                observeTransactionUseCase(id).collect { transaction ->
                    if (transaction != null) {
                        loadedTransaction = transaction
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                amountInput = transaction.amount.toPlainString().replace(".", ","),
                                description = transaction.description,
                                notes = transaction.notes.orEmpty(),
                                type = transaction.type,
                                selectedAccountId = transaction.accountId,
                                selectedCategoryId = transaction.categoryId,
                                transactionDate = DateUtils.instantToLocalDate(transaction.transactionDate),
                            )
                        }
                    }
                }
            }
        } ?: viewModelScope.launch {
            if (preselectedAccountId == null) {
                getDefaultAccountUseCase()?.let { default ->
                    _uiState.update { it.copy(selectedAccountId = default.id) }
                }
            }
        }
    }

    fun onAmountChange(value: String) {
        _uiState.update { it.copy(amountInput = value, errorMessage = null) }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value, errorMessage = null) }
    }

    fun onNotesChange(value: String) {
        _uiState.update { it.copy(notes = value) }
    }

    fun onTypeChange(type: TransactionType) {
        _uiState.update { it.copy(type = type) }
    }

    fun onAccountChange(accountId: Long) {
        _uiState.update { it.copy(selectedAccountId = accountId) }
    }

    fun onCategoryChange(categoryId: Long?) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun onDateChange(date: LocalDate) {
        _uiState.update { it.copy(transactionDate = date) }
    }

    fun save() {
        val state = _uiState.value
        val amount = MoneyFormatter.parse(state.amountInput)
            ?: state.amountInput.replace(",", ".").toBigDecimalOrNull()
        if (amount == null || amount <= BigDecimal.ZERO) {
            _uiState.update { it.copy(errorMessage = "Monto inválido") }
            return
        }
        val accountId = state.selectedAccountId
        if (accountId == null) {
            _uiState.update { it.copy(errorMessage = "Seleccioná una cuenta") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, duplicateWarning = null) }
            val transactionDate = DateUtils.localDateToInstant(state.transactionDate)

            if (state.transactionId == null) {
                when (
                    val result = addTransactionUseCase(
                        amount = amount,
                        type = state.type,
                        description = state.description,
                        accountId = accountId,
                        transactionDate = transactionDate,
                        notes = state.notes,
                        categoryId = state.selectedCategoryId,
                    )
                ) {
                    is DomainResult.Success -> {
                        val warning = if (result.data.duplicateCandidates.isNotEmpty()) {
                            "Posible duplicado: ${result.data.duplicateCandidates.size} movimiento(s) similar(es)"
                        } else {
                            null
                        }
                        _uiState.update {
                            it.copy(isSaving = false, savedSuccessfully = true, duplicateWarning = warning)
                        }
                    }
                    is DomainResult.Error -> _uiState.update {
                        it.copy(isSaving = false, errorMessage = result.message)
                    }
                }
            } else {
                val previous = loadedTransaction ?: return@launch
                val updated = previous.copy(
                    amount = amount.setScale(2, java.math.RoundingMode.HALF_UP),
                    type = state.type,
                    description = state.description.trim(),
                    categoryId = state.selectedCategoryId,
                    accountId = accountId,
                    transactionDate = transactionDate,
                    notes = state.notes.takeIf { it.isNotBlank() },
                )
                when (val result = updateTransactionUseCase(updated, previous)) {
                    is DomainResult.Success -> _uiState.update {
                        it.copy(isSaving = false, savedSuccessfully = true)
                    }
                    is DomainResult.Error -> _uiState.update {
                        it.copy(isSaving = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun delete() {
        val id = transactionId ?: return
        viewModelScope.launch {
            deleteTransactionUseCase(id)
            _uiState.update { it.copy(savedSuccessfully = true) }
        }
    }

    fun consumeSavedEvent() {
        _uiState.update { it.copy(savedSuccessfully = false) }
    }
}
