package com.fintrack.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument

data class MovementsListCallbacks(
    val onAddClick: (accountId: Long?) -> Unit,
    val onTransactionClick: (transactionId: Long) -> Unit,
)

data class MovementsGraphCallbacks(
    val list: @Composable (accountId: Long?, categoryId: Long?, callbacks: MovementsListCallbacks) -> Unit,
    val add: @Composable (onBack: () -> Unit, onSaved: () -> Unit, backStackEntry: NavBackStackEntry) -> Unit,
    val edit: @Composable (onBack: () -> Unit, onSaved: () -> Unit, backStackEntry: NavBackStackEntry) -> Unit,
)

data class SettingsGraphCallbacks(
    val home: @Composable (
        onNavigateToCategories: () -> Unit,
        onNavigateToRules: () -> Unit,
        onNavigateToLearned: () -> Unit,
    ) -> Unit,
    val categories: @Composable (onBack: () -> Unit) -> Unit,
    val classificationRules: @Composable (onBack: () -> Unit) -> Unit,
    val learnedMappings: @Composable (onBack: () -> Unit) -> Unit,
)

fun NavGraphBuilder.finTrackRootGraph(
    dashboard: @Composable () -> Unit,
    movements: MovementsGraphCallbacks,
    settings: SettingsGraphCallbacks,
    navigateUp: () -> Unit,
    navigateToCreateMovement: (accountId: Long?) -> Unit,
    navigateToEditMovement: (transactionId: Long) -> Unit,
    navigateToCategories: () -> Unit,
    navigateToClassificationRules: () -> Unit,
    navigateToClassificationLearned: () -> Unit,
) {
    composable(Routes.DASHBOARD) {
        dashboard()
    }

    movementsGraph(
        callbacks = movements,
        navigateUp = navigateUp,
        navigateToCreateMovement = navigateToCreateMovement,
        navigateToEditMovement = navigateToEditMovement,
    )

    settingsGraph(
        callbacks = settings,
        navigateUp = navigateUp,
        navigateToCategories = navigateToCategories,
        navigateToClassificationRules = navigateToClassificationRules,
        navigateToClassificationLearned = navigateToClassificationLearned,
    )
}

fun NavGraphBuilder.movementsGraph(
    callbacks: MovementsGraphCallbacks,
    navigateUp: () -> Unit,
    navigateToCreateMovement: (accountId: Long?) -> Unit,
    navigateToEditMovement: (transactionId: Long) -> Unit,
) {
    navigation(
        route = Routes.MOVEMENTS_GRAPH,
        startDestination = Routes.MOVEMENTS_LIST_BASE,
    ) {
        composable(
            route = Routes.MOVEMENTS_LIST,
            arguments = listOf(
                navArgument(NavArgs.ACCOUNT_ID) {
                    type = NavType.LongType
                    defaultValue = NavArgs.NONE
                },
                navArgument(NavArgs.CATEGORY_ID) {
                    type = NavType.LongType
                    defaultValue = NavArgs.NONE
                },
            ),
        ) { backStackEntry ->
            val (accountId, categoryId) = backStackEntry.parseMovementListFilters()
            callbacks.list(
                accountId,
                categoryId,
                MovementsListCallbacks(
                    onAddClick = { navigateToCreateMovement(accountId) },
                    onTransactionClick = navigateToEditMovement,
                ),
            )
        }

        composable(Routes.MOVEMENTS_LIST_BASE) {
            callbacks.list(
                null,
                null,
                MovementsListCallbacks(
                    onAddClick = { navigateToCreateMovement(null) },
                    onTransactionClick = navigateToEditMovement,
                ),
            )
        }

        composable(Routes.MOVEMENT_CREATE_BASE) { backStackEntry ->
            callbacks.add(navigateUp, navigateUp, backStackEntry)
        }

        composable(
            route = Routes.MOVEMENT_CREATE,
            arguments = listOf(
                navArgument(NavArgs.ACCOUNT_ID) { type = NavType.LongType },
            ),
        ) { backStackEntry ->
            callbacks.add(navigateUp, navigateUp, backStackEntry)
        }

        composable(
            route = Routes.MOVEMENT_EDIT,
            arguments = listOf(
                navArgument(NavArgs.TRANSACTION_ID) { type = NavType.LongType },
            ),
        ) { backStackEntry ->
            callbacks.edit(navigateUp, navigateUp, backStackEntry)
        }
    }
}

fun NavGraphBuilder.settingsGraph(
    callbacks: SettingsGraphCallbacks,
    navigateUp: () -> Unit,
    navigateToCategories: () -> Unit,
    navigateToClassificationRules: () -> Unit,
    navigateToClassificationLearned: () -> Unit,
) {
    navigation(
        route = Routes.SETTINGS_GRAPH,
        startDestination = Routes.SETTINGS_HOME,
    ) {
        composable(Routes.SETTINGS_HOME) {
            callbacks.home(
                navigateToCategories,
                navigateToClassificationRules,
                navigateToClassificationLearned,
            )
        }

        composable(Routes.SETTINGS_CATEGORIES) {
            callbacks.categories(navigateUp)
        }

        composable(Routes.SETTINGS_CLASSIFICATION_RULES) {
            callbacks.classificationRules(navigateUp)
        }

        composable(Routes.SETTINGS_CLASSIFICATION_LEARNED) {
            callbacks.learnedMappings(navigateUp)
        }
    }
}

fun NavBackStackEntry.parseMovementListFilters(): Pair<Long?, Long?> {
    val accountId = arguments?.getLong(NavArgs.ACCOUNT_ID)?.takeIf { it != NavArgs.NONE }
    val categoryId = arguments?.getLong(NavArgs.CATEGORY_ID)?.takeIf { it != NavArgs.NONE }
    return accountId to categoryId
}
