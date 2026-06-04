package com.fintrack.app.feature.transactions.presentation.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.core.common.DateUtils
import com.fintrack.core.common.MoneyFormatter
import com.fintrack.core.domain.common.DomainResult
import com.fintrack.core.domain.model.Transaction
import com.fintrack.core.domain.transaction.TransactionValidator
import com.fintrack.core.domain.usecase.account.GetDefaultAccountUseCase
import com.fintrack.core.domain.usecase.account.ObserveAccountsUseCase
import com.fintrack.core.domain.usecase.category.ObserveRootCategoriesUseCase
import com.fintrack.core.domain.usecase.transaction.AddTransactionUseCase
import com.fintrack.core.domain.usecase.transaction.DeleteTransactionUseCase
import com.fintrack.core.domain.usecase.transaction.ObserveTransactionUseCase
import com.fintrack.core.domain.usecase.transaction.UpdateTransactionUseCase
import com.fintrack.core.navigation.NavArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

private const val ERROR_INVALID_AMOUNT = "El monto debe ser mayor a cero"
private const val ERROR_DESCRIPTION_REQUIRED = "La descripción es obligatoria"
private const val ERROR_ACCOUNT_REQUIRED = "Seleccioná una cuenta"

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
    private val mode = if (transactionId == null) TransactionFormMode.Create else TransactionFormMode.Edit

    private val initialDate = DateUtils.instantToLocalDate(Instant.now())

    private val _uiState = MutableStateFlow(
        TransactionFormUiState(
            mode = mode,
            transactionId = transactionId,
            transactionDate = initialDate,
            formattedDate = formatDate(initialDate),
            isLoading = mode == TransactionFormMode.Edit,
        ),
    )
    val uiState: StateFlow<TransactionFormUiState> = _uiState.asStateFlow()

    private val effectsChannel = Channel<TransactionFormUiEffect>(Channel.BUFFERED)
    val uiEffect = effectsChannel.receiveAsFlow()

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
                observeTransactionUseCase(id).collect { transaction ->
                    if (transaction != null) {
                        loadedTransaction = transaction
                        val date = DateUtils.instantToLocalDate(transaction.transactionDate)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                amountInput = transaction.amount.toPlainString().replace(".", ","),
                                description = transaction.description,
                                notes = transaction.notes.orEmpty(),
                                type = transaction.type,
                                selectedAccountId = transaction.accountId,
                                selectedCategoryId = transaction.categoryId,
                                transactionDate = date,
                                formattedDate = formatDate(date),
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

    fun onEvent(event: TransactionFormUserEvent) {
        when (event) {
            is TransactionFormUserEvent.AmountChanged -> _uiState.update {
                it.copy(amountInput = event.value, amountError = null, generalError = null)
            }
            is TransactionFormUserEvent.DescriptionChanged -> _uiState.update {
                it.copy(description = event.value, descriptionError = null, generalError = null)
            }
            is TransactionFormUserEvent.NotesChanged -> _uiState.update {
                it.copy(notes = event.value)
            }
            is TransactionFormUserEvent.TypeChanged -> _uiState.update {
                it.copy(type = event.type)
            }
            is TransactionFormUserEvent.CategoryChanged -> _uiState.update {
                it.copy(selectedCategoryId = event.categoryId)
            }
            is TransactionFormUserEvent.AccountChanged -> _uiState.update {
                it.copy(selectedAccountId = event.accountId, accountError = null, generalError = null)
            }
            is TransactionFormUserEvent.DateChanged -> {
                _uiState.update {
                    it.copy(
                        transactionDate = event.date,
                        formattedDate = formatDate(event.date),
                        isDatePickerVisible = false,
                    )
                }
            }
            TransactionFormUserEvent.DatePickerRequested -> _uiState.update {
                it.copy(isDatePickerVisible = true)
            }
            TransactionFormUserEvent.DatePickerDismissed -> _uiState.update {
                it.copy(isDatePickerVisible = false)
            }
            TransactionFormUserEvent.SaveClicked -> save()
            TransactionFormUserEvent.DeleteRequested -> _uiState.update {
                it.copy(showDeleteDialog = true)
            }
            TransactionFormUserEvent.DeleteConfirmed -> confirmDelete()
            TransactionFormUserEvent.DeleteDismissed -> _uiState.update {
                it.copy(showDeleteDialog = false)
            }
        }
    }

    private fun save() {
        val state = _uiState.value
        val validation = validateForm(state)
        if (!validation.isValid) {
            _uiState.update {
                it.copy(
                    amountError = validation.amountError,
                    descriptionError = validation.descriptionError,
                    accountError = validation.accountError,
                )
            }
            return
        }

        val amount = validation.amount!!
        val accountId = state.selectedAccountId!!

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    generalError = null,
                    amountError = null,
                    descriptionError = null,
                    accountError = null,
                )
            }
            val transactionDate = DateUtils.localDateToInstant(state.transactionDate)

            if (state.mode == TransactionFormMode.Create) {
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
                        _uiState.update { it.copy(isSaving = false) }
                        val duplicateCount = result.data.duplicateCandidates.size
                        if (duplicateCount > 0) {
                            effectsChannel.send(TransactionFormUiEffect.ShowDuplicateWarning(duplicateCount))
                        }
                        effectsChannel.send(TransactionFormUiEffect.NavigateBack)
                    }
                    is DomainResult.Error -> _uiState.update {
                        it.copy(isSaving = false, generalError = result.message)
                    }
                }
            } else {
                val previous = loadedTransaction ?: run {
                    _uiState.update { it.copy(isSaving = false) }
                    return@launch
                }
                val updated = previous.copy(
                    amount = amount.setScale(2, RoundingMode.HALF_UP),
                    type = state.type,
                    description = state.description.trim(),
                    categoryId = state.selectedCategoryId,
                    accountId = accountId,
                    transactionDate = transactionDate,
                    notes = state.notes.takeIf { it.isNotBlank() },
                )
                when (val result = updateTransactionUseCase(updated, previous)) {
                    is DomainResult.Success -> {
                        _uiState.update { it.copy(isSaving = false) }
                        effectsChannel.send(TransactionFormUiEffect.NavigateBack)
                    }
                    is DomainResult.Error -> _uiState.update {
                        it.copy(isSaving = false, generalError = result.message)
                    }
                }
            }
        }
    }

    private fun confirmDelete() {
        val id = transactionId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(showDeleteDialog = false, isSaving = true) }
            when (val result = deleteTransactionUseCase(id)) {
                is DomainResult.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    effectsChannel.send(TransactionFormUiEffect.NavigateBack)
                }
                is DomainResult.Error -> _uiState.update {
                    it.copy(isSaving = false, generalError = result.message)
                }
            }
        }
    }

    private data class FormValidation(
        val isValid: Boolean,
        val amount: BigDecimal? = null,
        val amountError: String? = null,
        val descriptionError: String? = null,
        val accountError: String? = null,
    )

    private fun validateForm(state: TransactionFormUiState): FormValidation {
        val amount = MoneyFormatter.parse(state.amountInput)
            ?: state.amountInput.replace(",", ".").toBigDecimalOrNull()

        if (amount == null || amount <= BigDecimal.ZERO) {
            return FormValidation(
                isValid = false,
                amountError = ERROR_INVALID_AMOUNT,
            )
        }

        when (val domainValidation = TransactionValidator.validateManualEntry(state.description, amount)) {
            is DomainResult.Error -> {
                return FormValidation(
                    isValid = false,
                    amountError = if (domainValidation.message == ERROR_INVALID_AMOUNT) {
                        ERROR_INVALID_AMOUNT
                    } else {
                        null
                    },
                    descriptionError = if (domainValidation.message == ERROR_DESCRIPTION_REQUIRED) {
                        ERROR_DESCRIPTION_REQUIRED
                    } else {
                        null
                    },
                )
            }
            is DomainResult.Success -> Unit
        }

        if (state.selectedAccountId == null) {
            return FormValidation(
                isValid = false,
                accountError = ERROR_ACCOUNT_REQUIRED,
            )
        }

        return FormValidation(isValid = true, amount = amount)
    }

    private fun formatDate(date: LocalDate): String =
        DateUtils.formatDate(DateUtils.localDateToInstant(date))
}
