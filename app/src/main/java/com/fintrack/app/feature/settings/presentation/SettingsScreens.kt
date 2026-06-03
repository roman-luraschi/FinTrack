package com.fintrack.app.feature.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.R
import com.fintrack.core.designsystem.components.EmptyStateMessage
import com.fintrack.core.designsystem.components.ErrorMessage
import com.fintrack.core.designsystem.components.FinTrackTopBar
import com.fintrack.core.domain.model.AccountType

@Composable
fun SettingsScreen(
    onNavigateToRules: () -> Unit,
    onNavigateToLearned: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var accountName by rememberSaveable { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize()) {
        FinTrackTopBar(title = stringResource(R.string.nav_settings))
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text("Umbral fuzzy: ${"%.2f".format(uiState.fuzzyThreshold)}")
                Slider(
                    value = uiState.fuzzyThreshold,
                    onValueChange = viewModel::setFuzzyThreshold,
                    valueRange = 0.5f..1.0f,
                )
            }
            item {
                Text("Cuentas", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = accountName,
                    onValueChange = { accountName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre de cuenta") },
                )
                Button(
                    onClick = {
                        viewModel.addAccount(accountName, AccountType.BANK)
                        accountName = ""
                    },
                ) {
                    Text("Agregar cuenta")
                }
            }
            items(uiState.accounts, key = { it.id }) { account ->
                Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Text(text = "${account.name}${if (account.isDefault) " (default)" else ""}")
                    TextButton(onClick = { viewModel.setDefaultAccount(account.id) }) {
                        Text("Predeterminada")
                    }
                    if (!account.isDefault) {
                        TextButton(onClick = { viewModel.deleteAccount(account.id) }) {
                            Text(stringResource(R.string.delete))
                        }
                    }
                }
            }
            item {
                Text(
                    text = "Reglas de clasificación",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToRules)
                        .padding(12.dp),
                )
            }
            item {
                Text(
                    text = "Aprendizajes del usuario",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToLearned)
                        .padding(12.dp),
                )
            }
            uiState.errorMessage?.let { item { ErrorMessage(it) } }
            uiState.message?.let { item { Text(it) } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassificationRulesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClassificationRulesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var pattern by rememberSaveable { mutableStateOf("") }
    var selectedCategoryId by rememberSaveable { mutableStateOf<Long?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        FinTrackTopBar(
            title = "Reglas",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            },
        )
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = pattern,
                onValueChange = { pattern = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Patrón") },
            )
            uiState.categories.take(5).forEach { category ->
                TextButton(onClick = { selectedCategoryId = category.id }) {
                    Text("${category.name}${if (selectedCategoryId == category.id) " ✓" else ""}")
                }
            }
            Button(
                onClick = {
                    selectedCategoryId?.let {
                        viewModel.addRule(pattern, it)
                        pattern = ""
                    }
                },
            ) { Text("Agregar regla") }
        }
        if (uiState.rules.isEmpty()) {
            EmptyStateMessage(message = stringResource(R.string.empty_rules))
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                items(uiState.rules, key = { it.id }) { rule ->
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("${rule.pattern} → cat ${rule.categoryId}")
                        TextButton(onClick = { viewModel.deleteRule(rule.id) }) {
                            Text(stringResource(R.string.delete))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnedMappingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LearnedMappingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val categoryMap = uiState.categories.associateBy { it.id }

    Column(modifier = modifier.fillMaxSize()) {
        FinTrackTopBar(
            title = "Aprendizajes",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            },
        )
        if (uiState.mappings.isEmpty()) {
            EmptyStateMessage(message = stringResource(R.string.empty_learned))
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                items(uiState.mappings, key = { it.id }) { mapping ->
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("${mapping.merchantNormalized} → ${categoryMap[mapping.categoryId]?.name ?: mapping.categoryId}")
                        TextButton(onClick = { viewModel.revert(mapping.id) }) {
                            Text("Revertir")
                        }
                    }
                }
            }
        }
    }
}
