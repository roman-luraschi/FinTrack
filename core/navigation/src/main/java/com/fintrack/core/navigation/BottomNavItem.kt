package com.fintrack.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    Dashboard(Routes.DASHBOARD, "Inicio", Icons.Default.Dashboard),
    Movements(Routes.MOVEMENTS_GRAPH, "Movimientos", Icons.Default.AccountBalance),
    Settings(Routes.SETTINGS_GRAPH, "Ajustes", Icons.Default.Settings),
}
