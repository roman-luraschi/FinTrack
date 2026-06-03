package com.fintrack.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fintrack.app.feature.categories.presentation.CategoriesScreen
import com.fintrack.app.feature.dashboard.presentation.DashboardScreen
import com.fintrack.app.feature.settings.presentation.ClassificationRulesScreen
import com.fintrack.app.feature.settings.presentation.LearnedMappingsScreen
import com.fintrack.app.feature.settings.presentation.SettingsScreen
import com.fintrack.app.feature.transactions.presentation.TransactionDetailScreen
import com.fintrack.app.feature.transactions.presentation.TransactionFormScreen
import com.fintrack.app.feature.transactions.presentation.TransactionListScreen
import com.fintrack.core.navigation.BottomNavItem
import com.fintrack.core.navigation.Routes

@Composable
fun FinTrackNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in BottomNavItem.entries.map { it.route }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    BottomNavItem.entries.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute?.startsWith(item.route) == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    onCategoryClick = { categoryId ->
                        navController.navigate(Routes.transactions(categoryId = categoryId))
                    },
                )
            }
            composable(
                route = Routes.TRANSACTIONS,
                arguments = listOf(
                    navArgument("accountId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                    navArgument("categoryId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                ),
            ) { backStackEntry ->
                val accountId = backStackEntry.arguments?.getLong("accountId")?.takeIf { it >= 0 }
                val categoryId = backStackEntry.arguments?.getLong("categoryId")?.takeIf { it >= 0 }
                TransactionListScreen(
                    initialAccountId = accountId,
                    initialCategoryId = categoryId,
                    onAddClick = { navController.navigate(Routes.transactionAdd(accountId)) },
                    onTransactionClick = { id -> navController.navigate(Routes.transactionDetail(id)) },
                )
            }
            composable(Routes.TRANSACTIONS_BASE) {
                TransactionListScreen(
                    initialAccountId = null,
                    initialCategoryId = null,
                    onAddClick = { navController.navigate(Routes.transactionAdd()) },
                    onTransactionClick = { id -> navController.navigate(Routes.transactionDetail(id)) },
                )
            }
            composable(Routes.CATEGORIES) {
                CategoriesScreen()
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onNavigateToRules = { navController.navigate(Routes.CLASSIFICATION_RULES) },
                    onNavigateToLearned = { navController.navigate(Routes.CLASSIFICATION_LEARNED) },
                )
            }
            composable(
                route = Routes.TRANSACTION_ADD,
                arguments = listOf(
                    navArgument("accountId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                ),
            ) {
                TransactionFormScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }
            composable(
                route = Routes.TRANSACTION_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
            ) { backStackEntry ->
                TransactionDetailScreen(
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate(Routes.transactionEdit(id)) },
                )
            }
            composable(
                route = Routes.TRANSACTION_EDIT,
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
            ) {
                TransactionFormScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }
            composable(Routes.CLASSIFICATION_RULES) {
                ClassificationRulesScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.CLASSIFICATION_LEARNED) {
                LearnedMappingsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
