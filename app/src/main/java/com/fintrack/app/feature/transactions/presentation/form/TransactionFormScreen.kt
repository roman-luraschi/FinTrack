package com.fintrack.app.feature.transactions.presentation.form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.R
import com.fintrack.core.common.DateUtils
import com.fintrack.core.designsystem.components.ErrorMessage
import com.fintrack.core.designsystem.components.FinTrackTopBar
import com.fintrack.core.domain.model.Account
import com.fintrack.core.domain.model.Category
import com.fintrack.core.domain.model.TransactionType
import java.time.Instant
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionFormScreen(
    mode: TransactionFormMode,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val duplicateWarningTemplate = stringResource(R.string.duplicate_warning_detail)

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is TransactionFormUiEffect.ShowDuplicateWarning -> {
                    snackbarHostState.showSnackbar(
                        message = duplicateWarningTemplate.format(effect.count),
                    )
                }
                TransactionFormUiEffect.NavigateBack -> onSaved()
            }
        }
    }

    if (uiState.showDeleteDialog) {
        DeleteConfirmationDialog(
            isDeleting = uiState.isSaving,
            onConfirm = { viewModel.onEvent(TransactionFormUserEvent.DeleteConfirmed) },
            onDismiss = { viewModel.onEvent(TransactionFormUserEvent.DeleteDismissed) },
        )
    }

    if (uiState.isDatePickerVisible) {
        TransactionDatePickerDialog(
            selectedDate = uiState.transactionDate,
            onDateSelected = { viewModel.onEvent(TransactionFormUserEvent.DateChanged(it)) },
            onDismiss = { viewModel.onEvent(TransactionFormUserEvent.DatePickerDismissed) },
        )
    }

    val title = when (mode) {
        TransactionFormMode.Create -> stringResource(R.string.add_transaction)
        TransactionFormMode.Edit -> stringResource(R.string.edit_transaction)
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            FinTrackTopBar(
                title = title,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                TransactionFormContent(
                    uiState = uiState,
                    onEvent = viewModel::onEvent,
                    showDeleteButton = mode == TransactionFormMode.Edit,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TransactionFormContent(
    uiState: TransactionFormUiState,
    onEvent: (TransactionFormUserEvent) -> Unit,
    showDeleteButton: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TransactionTypeSelector(
            selectedType = uiState.type,
            onTypeSelected = { onEvent(TransactionFormUserEvent.TypeChanged(it)) },
        )
        OutlinedTextField(
            value = uiState.amountInput,
            onValueChange = { onEvent(TransactionFormUserEvent.AmountChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.field_amount)) },
            singleLine = true,
            isError = uiState.amountError != null,
            supportingText = uiState.amountError?.let { { Text(it) } },
        )
        OutlinedTextField(
            value = uiState.description,
            onValueChange = { onEvent(TransactionFormUserEvent.DescriptionChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.field_description)) },
            isError = uiState.descriptionError != null,
            supportingText = uiState.descriptionError?.let { { Text(it) } },
        )
        TransactionDateField(
            formattedDate = uiState.formattedDate,
            onClick = { onEvent(TransactionFormUserEvent.DatePickerRequested) },
        )
        if (uiState.categories.isNotEmpty()) {
            CategorySelector(
                categories = uiState.categories,
                selectedCategoryId = uiState.selectedCategoryId,
                onCategorySelected = { onEvent(TransactionFormUserEvent.CategoryChanged(it)) },
            )
        }
        if (uiState.accounts.isNotEmpty()) {
            AccountSelector(
                accounts = uiState.accounts,
                selectedAccountId = uiState.selectedAccountId,
                onAccountSelected = { onEvent(TransactionFormUserEvent.AccountChanged(it)) },
                accountError = uiState.accountError,
            )
        }
        OutlinedTextField(
            value = uiState.notes,
            onValueChange = { onEvent(TransactionFormUserEvent.NotesChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.field_notes_optional)) },
            minLines = 2,
        )
        uiState.generalError?.let { ErrorMessage(it) }
        Button(
            onClick = { onEvent(TransactionFormUserEvent.SaveClicked) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSaving,
        ) {
            Text(stringResource(R.string.save))
        }
        if (showDeleteButton) {
            TextButton(
                onClick = { onEvent(TransactionFormUserEvent.DeleteRequested) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving,
            ) {
                Text(stringResource(R.string.delete))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TransactionTypeSelector(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selectedType == TransactionType.EXPENSE,
            onClick = { onTypeSelected(TransactionType.EXPENSE) },
            label = { Text(stringResource(R.string.expense)) },
        )
        FilterChip(
            selected = selectedType == TransactionType.INCOME,
            onClick = { onTypeSelected(TransactionType.INCOME) },
            label = { Text(stringResource(R.string.income)) },
        )
    }
}

@Composable
private fun TransactionDateField(
    formattedDate: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = formattedDate,
        onValueChange = {},
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        readOnly = true,
        label = { Text(stringResource(R.string.field_date)) },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategorySelector(
    categories: List<Category>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = stringResource(R.string.field_category))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = selectedCategoryId == null,
                onClick = { onCategorySelected(null) },
                label = { Text(stringResource(R.string.category_none)) },
            )
            categories.forEach { category ->
                FilterChip(
                    selected = selectedCategoryId == category.id,
                    onClick = { onCategorySelected(category.id) },
                    label = { Text(category.name) },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccountSelector(
    accounts: List<Account>,
    selectedAccountId: Long?,
    onAccountSelected: (Long) -> Unit,
    accountError: String?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = stringResource(R.string.field_account))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            accounts.forEach { account ->
                FilterChip(
                    selected = selectedAccountId == account.id,
                    onClick = { onAccountSelected(account.id) },
                    label = { Text(account.name) },
                )
            }
        }
        accountError?.let { ErrorMessage(it) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionDatePickerDialog(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate
            .atStartOfDay(DateUtils.argentinaZone)
            .toInstant()
            .toEpochMilli(),
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(DateUtils.argentinaZone)
                            .toLocalDate()
                        onDateSelected(date)
                    } ?: onDismiss()
                },
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
private fun DeleteConfirmationDialog(
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_movement_title)) },
        text = { Text(stringResource(R.string.delete_movement_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isDeleting) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isDeleting) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        FinTrackTopBar(
            title = stringResource(R.string.transaction_detail),
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                    )
                }
            },
            actions = {
                uiState.transactionId?.let { id ->
                    TextButton(onClick = { onEdit(id) }) {
                        Text(stringResource(R.string.edit))
                    }
                }
            },
        )
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = uiState.description)
            Text(text = uiState.amountInput)
            uiState.selectedCategoryId?.let { catId ->
                uiState.categories.find { it.id == catId }?.name?.let { name ->
                    Text(text = "${stringResource(R.string.field_category)}: $name")
                }
            }
            uiState.accounts.find { it.id == uiState.selectedAccountId }?.name?.let { name ->
                Text(text = "${stringResource(R.string.field_account)}: $name")
            }
            if (uiState.notes.isNotBlank()) {
                Text(text = uiState.notes)
            }
        }
    }
}
