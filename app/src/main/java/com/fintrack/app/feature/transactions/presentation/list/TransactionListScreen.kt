package com.fintrack.app.feature.transactions.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.R
import com.fintrack.core.designsystem.components.AmountText
import com.fintrack.core.designsystem.components.CategoryChip
import com.fintrack.core.designsystem.components.EmptyStateMessage
import com.fintrack.core.designsystem.components.ErrorMessage
import com.fintrack.core.designsystem.components.FinTrackTopBar
import com.fintrack.core.domain.model.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    initialAccountId: Long?,
    initialCategoryId: Long?,
    onAddClick: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val movementDeletedMessage = stringResource(R.string.movement_deleted)
    val addMovementDescription = stringResource(R.string.add_movement)

    LaunchedEffect(initialAccountId, initialCategoryId) {
        viewModel.onEvent(
            TransactionListUserEvent.ApplyNavArgs(
                accountId = initialAccountId,
                categoryId = initialCategoryId,
            ),
        )
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is TransactionListUiEffect.NavigateToEdit -> onTransactionClick(effect.transactionId)
                TransactionListUiEffect.MovementDeleted ->
                    snackbarHostState.showSnackbar(movementDeletedMessage)
                is TransactionListUiEffect.ShowError ->
                    snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    if (uiState.pendingDeleteId != null) {
        DeleteConfirmationDialog(
            isDeleting = uiState.isDeleting,
            onConfirm = { viewModel.onEvent(TransactionListUserEvent.DeleteConfirmed) },
            onDismiss = { viewModel.onEvent(TransactionListUserEvent.DeleteDismissed) },
        )
    }

    if (uiState.isFilterSheetVisible) {
        FilterBottomSheet(
            uiState = uiState,
            onDismiss = { viewModel.onEvent(TransactionListUserEvent.ToggleFilterSheet) },
            onAccountSelected = {
                viewModel.onEvent(TransactionListUserEvent.AccountFilterChanged(it))
            },
            onCategorySelected = {
                viewModel.onEvent(TransactionListUserEvent.CategoryFilterChanged(it))
            },
            onTypeSelected = {
                viewModel.onEvent(TransactionListUserEvent.TypeFilterChanged(it))
            },
            onApply = { viewModel.onEvent(TransactionListUserEvent.ToggleFilterSheet) },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                modifier = Modifier.semantics {
                    contentDescription = addMovementDescription
                },
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            FinTrackTopBar(
                title = stringResource(R.string.nav_transactions),
                actions = {
                    val filterContentDescription = stringResource(R.string.filter_title)
                    if (uiState.hasActiveFilters) {
                        BadgedBox(
                            badge = { Badge() },
                        ) {
                            IconButton(
                                onClick = { viewModel.onEvent(TransactionListUserEvent.ToggleFilterSheet) },
                                modifier = Modifier.semantics { contentDescription = filterContentDescription },
                            ) {
                                Icon(Icons.Default.FilterList, contentDescription = null)
                            }
                        }
                    } else {
                        IconButton(
                            onClick = { viewModel.onEvent(TransactionListUserEvent.ToggleFilterSheet) },
                            modifier = Modifier.semantics { contentDescription = filterContentDescription },
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = null)
                        }
                    }
                },
            )
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onEvent(TransactionListUserEvent.SearchQueryChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.search_hint)) },
                singleLine = true,
            )
            TypeFilterRow(
                selectedType = uiState.selectedType,
                onTypeSelected = { viewModel.onEvent(TransactionListUserEvent.TypeFilterChanged(it)) },
            )
            ActiveFilterChips(
                uiState = uiState,
                onClearAccount = {
                    viewModel.onEvent(TransactionListUserEvent.AccountFilterChanged(null))
                },
                onClearCategory = {
                    viewModel.onEvent(TransactionListUserEvent.CategoryFilterChanged(null))
                },
                onClearAll = { viewModel.onEvent(TransactionListUserEvent.ClearFilters) },
            )
            when {
                uiState.errorMessage != null -> ErrorMessage(uiState.errorMessage!!)
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.items.isEmpty() -> {
                    val emptyMessage = if (uiState.isFilteredEmpty) {
                        stringResource(R.string.empty_transactions_filtered)
                    } else {
                        stringResource(R.string.empty_transactions)
                    }
                    EmptyStateMessage(message = emptyMessage)
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(uiState.items, key = { it.id }) { item ->
                            MovementRow(
                                item = item,
                                onClick = {
                                    viewModel.onEvent(TransactionListUserEvent.MovementClicked(item.id))
                                },
                                onDelete = {
                                    viewModel.onEvent(TransactionListUserEvent.DeleteRequested(item.id))
                                },
                            )
                        }
                    }
                }
            }
        }
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
private fun FilterBottomSheet(
    uiState: TransactionListUiState,
    onDismiss: () -> Unit,
    onAccountSelected: (Long?) -> Unit,
    onCategorySelected: (Long?) -> Unit,
    onTypeSelected: (TransactionType?) -> Unit,
    onApply: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.filter_title),
                style = MaterialTheme.typography.titleLarge,
            )
            FilterSection(
                title = stringResource(R.string.filter_account),
                options = uiState.accounts,
                selectedId = uiState.selectedAccountId,
                labelFor = { it.name },
                idFor = { it.id },
                onSelected = onAccountSelected,
            )
            FilterSection(
                title = stringResource(R.string.filter_category),
                options = uiState.categories,
                selectedId = uiState.selectedCategoryId,
                labelFor = { it.name },
                idFor = { it.id },
                onSelected = onCategorySelected,
            )
            Text(
                text = stringResource(R.string.filter_type),
                style = MaterialTheme.typography.labelLarge,
            )
            TypeFilterRow(
                selectedType = uiState.selectedType,
                onTypeSelected = onTypeSelected,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onApply) {
                    Text(stringResource(R.string.apply_filters))
                }
            }
        }
    }
}

@Composable
private fun <T> FilterSection(
    title: String,
    options: List<T>,
    selectedId: Long?,
    labelFor: (T) -> String,
    idFor: (T) -> Long,
    onSelected: (Long?) -> Unit,
) {
    Text(text = title, style = MaterialTheme.typography.labelLarge)
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        FilterChip(
            selected = selectedId == null,
            onClick = { onSelected(null) },
            label = { Text(stringResource(R.string.filter_all)) },
        )
        options.forEach { option ->
            val id = idFor(option)
            FilterChip(
                selected = selectedId == id,
                onClick = { onSelected(id) },
                label = { Text(labelFor(option)) },
            )
        }
    }
}

@Composable
private fun TypeFilterRow(
    selectedType: TransactionType?,
    onTypeSelected: (TransactionType?) -> Unit,
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selectedType == null,
            onClick = { onTypeSelected(null) },
            label = { Text(stringResource(R.string.filter_type_all)) },
        )
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
private fun ActiveFilterChips(
    uiState: TransactionListUiState,
    onClearAccount: () -> Unit,
    onClearCategory: () -> Unit,
    onClearAll: () -> Unit,
) {
    if (!uiState.hasActiveFilters) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        uiState.selectedAccountName?.let { name ->
            FilterChip(
                selected = true,
                onClick = onClearAccount,
                label = { Text(name) },
            )
        }
        uiState.selectedCategoryName?.let { name ->
            FilterChip(
                selected = true,
                onClick = onClearCategory,
                label = { Text(name) },
            )
        }
        TextButton(onClick = onClearAll) {
            Text(stringResource(R.string.clear_filters))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovementRow(
    item: TransactionListItem,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
            }
            false
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = item.description)
                    item.categoryName?.let { CategoryChip(label = it) }
                    Text(text = item.dateLabel)
                    if (item.needsReview) {
                        Text(
                            text = stringResource(R.string.needs_review),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }
                AmountText(
                    amount = if (item.isExpense) "- ${item.formattedAmount}" else "+ ${item.formattedAmount}",
                    isExpense = item.isExpense,
                )
            }
        },
    )
}
