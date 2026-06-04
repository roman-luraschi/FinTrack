package com.fintrack.app.feature.transactions.presentation.form

import com.fintrack.core.domain.model.Account
import com.fintrack.core.domain.model.Category
import com.fintrack.core.domain.model.TransactionType
import java.time.LocalDate

enum class TransactionFormMode {
    Create,
    Edit,
}

data class TransactionFormUiState(
    val mode: TransactionFormMode,
    val transactionId: Long? = null,
    val amountInput: String = "",
    val description: String = "",
    val notes: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val selectedAccountId: Long? = null,
    val selectedCategoryId: Long? = null,
    val transactionDate: LocalDate,
    val formattedDate: String = "",
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDatePickerVisible: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val amountError: String? = null,
    val descriptionError: String? = null,
    val accountError: String? = null,
    val generalError: String? = null,
)

sealed interface TransactionFormUserEvent {
    data class AmountChanged(val value: String) : TransactionFormUserEvent
    data class DescriptionChanged(val value: String) : TransactionFormUserEvent
    data class NotesChanged(val value: String) : TransactionFormUserEvent
    data class TypeChanged(val type: TransactionType) : TransactionFormUserEvent
    data class CategoryChanged(val categoryId: Long?) : TransactionFormUserEvent
    data class AccountChanged(val accountId: Long) : TransactionFormUserEvent
    data class DateChanged(val date: LocalDate) : TransactionFormUserEvent
    data object DatePickerRequested : TransactionFormUserEvent
    data object DatePickerDismissed : TransactionFormUserEvent
    data object SaveClicked : TransactionFormUserEvent
    data object DeleteRequested : TransactionFormUserEvent
    data object DeleteConfirmed : TransactionFormUserEvent
    data object DeleteDismissed : TransactionFormUserEvent
}

sealed interface TransactionFormUiEffect {
    data object NavigateBack : TransactionFormUiEffect
    data class ShowDuplicateWarning(val count: Int) : TransactionFormUiEffect
}
