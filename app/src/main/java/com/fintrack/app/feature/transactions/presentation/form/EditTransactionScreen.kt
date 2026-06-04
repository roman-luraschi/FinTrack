package com.fintrack.app.feature.transactions.presentation.form

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry

@Composable
fun EditTransactionScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    backStackEntry: NavBackStackEntry,
    modifier: Modifier = Modifier,
) {
    TransactionFormScreen(
        mode = TransactionFormMode.Edit,
        onBack = onBack,
        onSaved = onSaved,
        modifier = modifier,
        viewModel = hiltViewModel(backStackEntry),
    )
}
