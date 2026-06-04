package com.fintrack.app.feature.transactions.presentation.form

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun EditTransactionScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TransactionFormScreen(
        mode = TransactionFormMode.Edit,
        onBack = onBack,
        onSaved = onSaved,
        modifier = modifier,
    )
}
