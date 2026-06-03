package com.fintrack.app.feature.transactions.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.fintrack.core.designsystem.components.FinTrackTopBar
import com.fintrack.core.domain.model.TransactionType

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

    LaunchedEffect(initialAccountId, initialCategoryId) {
        viewModel.applyNavArgs(initialAccountId, initialCategoryId)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                modifier = Modifier.semantics { contentDescription = "Agregar movimiento" },
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
            FinTrackTopBar(title = stringResource(R.string.nav_transactions))
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.search_hint)) },
                singleLine = true,
            )
            FilterRow(
                selectedType = uiState.selectedType,
                onTypeSelected = viewModel::onTypeFilterChange,
            )
            if (uiState.items.isEmpty()) {
                EmptyStateMessage(message = stringResource(R.string.empty_transactions))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.items, key = { it.id }) { item ->
                        TransactionRow(item = item, onClick = { onTransactionClick(item.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(
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
            label = { Text("Todos") },
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
private fun TransactionRow(
    item: TransactionListItem,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.description)
            item.categoryName?.let { CategoryChip(label = it) }
            Text(text = item.dateLabel)
            if (item.needsReview) {
                Text(text = "Revisar categoría")
            }
        }
        AmountText(
            amount = if (item.isExpense) "- ${item.formattedAmount}" else "+ ${item.formattedAmount}",
            isExpense = item.isExpense,
        )
    }
}
