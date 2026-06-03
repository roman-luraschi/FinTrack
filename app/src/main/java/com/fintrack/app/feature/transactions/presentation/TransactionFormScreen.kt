package com.fintrack.app.feature.transactions.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.R
import com.fintrack.core.designsystem.components.ErrorMessage
import com.fintrack.core.designsystem.components.FinTrackTopBar
import com.fintrack.core.domain.model.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.savedSuccessfully) {
        if (uiState.savedSuccessfully) {
            viewModel.consumeSavedEvent()
            onSaved()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        FinTrackTopBar(
            title = if (uiState.transactionId == null) {
                stringResource(R.string.add_transaction)
            } else {
                "Editar movimiento"
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            },
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.type == TransactionType.EXPENSE,
                    onClick = { viewModel.onTypeChange(TransactionType.EXPENSE) },
                    label = { Text(stringResource(R.string.expense)) },
                )
                FilterChip(
                    selected = uiState.type == TransactionType.INCOME,
                    onClick = { viewModel.onTypeChange(TransactionType.INCOME) },
                    label = { Text(stringResource(R.string.income)) },
                )
            }
            OutlinedTextField(
                value = uiState.amountInput,
                onValueChange = viewModel::onAmountChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Monto") },
                singleLine = true,
            )
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Descripción") },
            )
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::onNotesChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notas (opcional)") },
            )
            if (uiState.categories.isNotEmpty()) {
                Text("Categoría")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.categories.take(6).forEach { category ->
                        FilterChip(
                            selected = uiState.selectedCategoryId == category.id,
                            onClick = { viewModel.onCategoryChange(category.id) },
                            label = { Text(category.name) },
                        )
                    }
                }
            }
            if (uiState.accounts.isNotEmpty()) {
                Text("Cuenta")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.accounts.forEach { account ->
                        FilterChip(
                            selected = uiState.selectedAccountId == account.id,
                            onClick = { viewModel.onAccountChange(account.id) },
                            label = { Text(account.name) },
                        )
                    }
                }
            }
            uiState.errorMessage?.let { ErrorMessage(it) }
            uiState.duplicateWarning?.let { Text(text = it) }
            Button(
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving,
            ) {
                Text(stringResource(R.string.save))
            }
            if (uiState.transactionId != null) {
                TextButton(
                    onClick = viewModel::delete,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.delete))
                }
            }
        }
    }
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
            title = "Detalle",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            },
            actions = {
                uiState.transactionId?.let { id ->
                    TextButton(onClick = { onEdit(id) }) {
                        Text("Editar")
                    }
                }
            },
        )
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = uiState.description)
            Text(text = uiState.amountInput)
            uiState.selectedCategoryId?.let { catId ->
                uiState.categories.find { it.id == catId }?.name?.let { Text(text = "Categoría: $it") }
            }
            uiState.accounts.find { it.id == uiState.selectedAccountId }?.name?.let {
                Text(text = "Cuenta: $it")
            }
            if (uiState.notes.isNotBlank()) Text(text = uiState.notes)
        }
    }
}
