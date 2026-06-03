package com.fintrack.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    Dashboard(Routes.DASHBOARD, "Inicio", Icons.Default.Dashboard),
    Transactions(Routes.TRANSACTIONS_BASE, "Movimientos", Icons.Default.AccountBalance),
    Categories(Routes.CATEGORIES, "Categorías", Icons.Default.Category),
    Settings(Routes.SETTINGS, "Ajustes", Icons.Default.Settings),
}
