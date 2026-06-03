package com.fintrack.app.feature.dashboard.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.R
import com.fintrack.core.common.MoneyFormatter
import com.fintrack.core.designsystem.components.EmptyStateMessage
import com.fintrack.core.designsystem.components.ErrorMessage
import com.fintrack.core.designsystem.components.FinTrackTopBar
import com.fintrack.core.domain.model.DashboardPeriod

@Composable
fun DashboardScreen(
    onCategoryClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        FinTrackTopBar(title = stringResource(R.string.nav_dashboard))
        PeriodSelector(
            selected = uiState.period,
            onSelect = viewModel::setPeriod,
        )
        when {
            uiState.errorMessage != null -> ErrorMessage(uiState.errorMessage!!)
            uiState.summary == null && uiState.isLoading -> {
                EmptyStateMessage(message = "Cargando…")
            }
            else -> {
                val summary = uiState.summary ?: return@Column
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        SummaryCard(
                            label = "Gastos",
                            amount = uiState.formattedExpenses,
                            contentDescription = "Total de gastos del período",
                        )
                    }
                    item {
                        SummaryCard(
                            label = "Ingresos",
                            amount = uiState.formattedIncome,
                            contentDescription = "Total de ingresos del período",
                        )
                    }
                    item {
                        SummaryCard(
                            label = "Balance",
                            amount = uiState.formattedBalance,
                            contentDescription = "Balance neto del período",
                        )
                    }
                    item {
                        Text(
                            text = "Gastos por categoría",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    if (summary.byCategory.isEmpty()) {
                        item { EmptyStateMessage(message = "Sin gastos en este período") }
                    } else {
                        items(summary.byCategory, key = { it.categoryId }) { item ->
                            CategoryBreakdownRow(
                                name = item.categoryName,
                                amount = MoneyFormatter.format(item.total),
                                onClick = { onCategoryClick(item.categoryId) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodSelector(
    selected: DashboardPeriod,
    onSelect: (DashboardPeriod) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selected == DashboardPeriod.MONTH,
                onClick = { onSelect(DashboardPeriod.MONTH) },
                label = { Text("Mes") },
            )
            FilterChip(
                selected = selected == DashboardPeriod.WEEK,
                onClick = { onSelect(DashboardPeriod.WEEK) },
                label = { Text("Semana") },
            )
        }
    }
}

@Composable
private fun SummaryCard(
    label: String,
    amount: String,
    contentDescription: String,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { this.contentDescription = contentDescription },
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelLarge)
            Text(text = amount, style = MaterialTheme.typography.headlineLarge)
        }
    }
}

@Composable
private fun CategoryBreakdownRow(
    name: String,
    amount: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = name, style = MaterialTheme.typography.bodyLarge)
            Text(text = amount, style = MaterialTheme.typography.titleLarge)
        }
    }
}
