package com.fintrack.app.feature.dashboard.presentation

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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.R
import com.fintrack.core.designsystem.components.AmountText
import com.fintrack.core.designsystem.components.CategoryChip
import com.fintrack.core.designsystem.components.EmptyStateMessage
import com.fintrack.core.designsystem.components.ErrorMessage
import com.fintrack.core.designsystem.components.FinTrackTopBar

@Composable
fun DashboardScreen(
    onCategoryClick: (Long) -> Unit,
    onMovementClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        FinTrackTopBar(title = stringResource(R.string.nav_dashboard))
        when {
            uiState.errorMessage != null -> ErrorMessage(uiState.errorMessage!!)
            uiState.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = stringResource(R.string.dashboard_loading),
                        modifier = Modifier.padding(top = 16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        MetricRow(
                            first = {
                                MetricCard(
                                    label = stringResource(R.string.dashboard_spent_today),
                                    value = uiState.formattedSpentToday,
                                    contentDescription = stringResource(R.string.dashboard_spent_today),
                                )
                            },
                            second = {
                                MetricCard(
                                    label = stringResource(R.string.dashboard_spent_month),
                                    value = uiState.formattedSpentThisMonth,
                                    contentDescription = stringResource(R.string.dashboard_spent_month),
                                )
                            },
                        )
                    }
                    item {
                        MetricRow(
                            first = {
                                MetricCard(
                                    label = stringResource(R.string.dashboard_movement_count),
                                    value = uiState.movementCount.toString(),
                                    subtitle = stringResource(R.string.dashboard_movement_count_subtitle),
                                    contentDescription = stringResource(R.string.dashboard_movement_count),
                                )
                            },
                            second = {
                                TopCategoryCard(
                                    categoryName = uiState.topCategoryName,
                                    formattedAmount = uiState.topCategoryFormattedAmount,
                                    categoryId = uiState.topCategoryId,
                                    onCategoryClick = onCategoryClick,
                                )
                            },
                        )
                    }
                    item {
                        Text(
                            text = stringResource(R.string.dashboard_recent_movements),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    if (uiState.recentMovements.isEmpty()) {
                        item {
                            EmptyStateMessage(message = stringResource(R.string.empty_transactions))
                        }
                    } else {
                        items(uiState.recentMovements, key = { it.id }) { item ->
                            RecentMovementRow(
                                item = item,
                                onClick = { onMovementClick(item.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricRow(
    first: @Composable () -> Unit,
    second: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) { first() }
        Column(modifier = Modifier.weight(1f)) { second() }
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    contentDescription: String,
    subtitle: String? = null,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { this.contentDescription = contentDescription },
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelLarge)
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun TopCategoryCard(
    categoryName: String?,
    formattedAmount: String?,
    categoryId: Long?,
    onCategoryClick: (Long) -> Unit,
) {
    val hasCategory = categoryName != null && formattedAmount != null && categoryId != null
    val topCategoryLabel = stringResource(R.string.dashboard_top_category)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (hasCategory) {
                    Modifier.clickable { onCategoryClick(categoryId!!) }
                } else {
                    Modifier
                },
            )
            .semantics { this.contentDescription = topCategoryLabel },
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.dashboard_top_category),
                style = MaterialTheme.typography.labelLarge,
            )
            if (hasCategory) {
                Text(
                    text = categoryName!!,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    text = formattedAmount!!,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
            } else {
                Text(
                    text = stringResource(R.string.dashboard_no_top_category),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Start,
                )
            }
        }
    }
}

@Composable
private fun RecentMovementRow(
    item: RecentMovementItem,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.description, style = MaterialTheme.typography.bodyLarge)
                item.categoryName?.let { CategoryChip(label = it) }
                Text(
                    text = item.dateLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AmountText(
                amount = if (item.isExpense) "- ${item.formattedAmount}" else "+ ${item.formattedAmount}",
                isExpense = item.isExpense,
            )
        }
    }
}
