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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fintrack.app.feature.categories.presentation.CategoriesScreen
import com.fintrack.app.feature.dashboard.presentation.DashboardScreen
import com.fintrack.app.feature.settings.presentation.ClassificationRulesScreen
import com.fintrack.app.feature.settings.presentation.LearnedMappingsScreen
import com.fintrack.app.feature.settings.presentation.SettingsScreen
import com.fintrack.app.feature.transactions.presentation.TransactionFormScreen
import com.fintrack.app.feature.transactions.presentation.list.TransactionListScreen
import com.fintrack.core.navigation.BottomNavItem
import com.fintrack.core.navigation.MovementsGraphCallbacks
import com.fintrack.core.navigation.Routes
import com.fintrack.core.navigation.SettingsGraphCallbacks
import com.fintrack.core.navigation.finTrackRootGraph
import com.fintrack.core.navigation.isBottomNavTabSelected
import com.fintrack.core.navigation.navigateToCategories
import com.fintrack.core.navigation.navigateToClassificationLearned
import com.fintrack.core.navigation.navigateToClassificationRules
import com.fintrack.core.navigation.navigateToCreateMovement
import com.fintrack.core.navigation.navigateToEditMovement
import com.fintrack.core.navigation.navigateToMovementsList
import com.fintrack.core.navigation.navigateUp
import com.fintrack.core.navigation.showsBottomBar

@Composable
fun FinTrackNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = currentDestination.showsBottomBar()

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    BottomNavItem.entries.forEach { item ->
                        NavigationBarItem(
                            selected = currentDestination.isBottomNavTabSelected(item.route),
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
            finTrackRootGraph(
                dashboard = {
                    DashboardScreen(
                        onCategoryClick = { categoryId ->
                            navController.navigateToMovementsList(categoryId = categoryId)
                        },
                        onMovementClick = { transactionId ->
                            navController.navigateToEditMovement(transactionId)
                        },
                    )
                },
                movements = MovementsGraphCallbacks(
                    list = { accountId, categoryId, callbacks ->
                        TransactionListScreen(
                            initialAccountId = accountId,
                            initialCategoryId = categoryId,
                            onAddClick = { callbacks.onAddClick(accountId) },
                            onTransactionClick = callbacks.onTransactionClick,
                        )
                    },
                    add = { onBack, onSaved ->
                        TransactionFormScreen(
                            onBack = onBack,
                            onSaved = onSaved,
                        )
                    },
                    edit = { onBack, onSaved ->
                        TransactionFormScreen(
                            onBack = onBack,
                            onSaved = onSaved,
                        )
                    },
                ),
                settings = SettingsGraphCallbacks(
                    home = { onNavigateToCategories, onNavigateToRules, onNavigateToLearned ->
                        SettingsScreen(
                            onNavigateToCategories = onNavigateToCategories,
                            onNavigateToRules = onNavigateToRules,
                            onNavigateToLearned = onNavigateToLearned,
                        )
                    },
                    categories = { onBack ->
                        CategoriesScreen(onBack = onBack)
                    },
                    classificationRules = { onBack ->
                        ClassificationRulesScreen(onBack = onBack)
                    },
                    learnedMappings = { onBack ->
                        LearnedMappingsScreen(onBack = onBack)
                    },
                ),
                navigateUp = { navController.navigateUp() },
                navigateToCreateMovement = navController::navigateToCreateMovement,
                navigateToEditMovement = navController::navigateToEditMovement,
                navigateToCategories = navController::navigateToCategories,
                navigateToClassificationRules = navController::navigateToClassificationRules,
                navigateToClassificationLearned = navController::navigateToClassificationLearned,
            )
        }
    }
}
