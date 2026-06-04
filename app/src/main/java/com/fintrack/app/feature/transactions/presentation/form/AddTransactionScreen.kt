package com.fintrack.app.feature.transactions.presentation.form

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AddTransactionScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TransactionFormScreen(
        mode = TransactionFormMode.Create,
        onBack = onBack,
        onSaved = onSaved,
        modifier = modifier,
    )
}
