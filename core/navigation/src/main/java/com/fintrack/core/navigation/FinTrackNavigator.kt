package com.fintrack.core.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavHostController

/**
 * Con subgrafos anidados, [NavHostController.navigate] solo resuelve rutas hijas del grafo
 * contenedor del destino actual. Desde dentro de `movements` hay que navegar a `create`,
 * no a `movements/create`. Desde fuera (p. ej. dashboard) se usa la ruta absoluta.
 */
private fun NavHostController.isInsideGraph(graphRoute: String): Boolean {
    var current: NavDestination? = currentDestination
    while (current != null) {
        if (current.route == graphRoute) return true
        current = current.parent
    }
    return false
}

private fun NavHostController.navigateInGraph(
    graphRoute: String,
    absoluteRoute: String,
    relativeRoute: String,
) {
    navigate(if (isInsideGraph(graphRoute)) relativeRoute else absoluteRoute)
}

fun NavHostController.navigateToDashboard() {
    navigate(FinTrackDestination.Dashboard.toRoute())
}

fun NavHostController.navigateToMovementsList(
    accountId: Long? = null,
    categoryId: Long? = null,
) {
    navigateInGraph(
        graphRoute = Routes.MOVEMENTS_GRAPH,
        absoluteRoute = Routes.movementsList(accountId, categoryId),
        relativeRoute = Routes.movementsListRelative(accountId, categoryId),
    )
}

fun NavHostController.navigateToCreateMovement(accountId: Long? = null) {
    navigateInGraph(
        graphRoute = Routes.MOVEMENTS_GRAPH,
        absoluteRoute = Routes.movementCreate(accountId),
        relativeRoute = Routes.movementCreateRelative(accountId),
    )
}

fun NavHostController.navigateToEditMovement(transactionId: Long) {
    navigateInGraph(
        graphRoute = Routes.MOVEMENTS_GRAPH,
        absoluteRoute = Routes.movementEdit(transactionId),
        relativeRoute = Routes.movementEditRelative(transactionId),
    )
}

fun NavHostController.navigateToSettings() {
    navigate(FinTrackDestination.Settings.toRoute())
}

fun NavHostController.navigateToCategories() {
    navigateInGraph(
        graphRoute = Routes.SETTINGS_GRAPH,
        absoluteRoute = Routes.settingsCategories(),
        relativeRoute = Routes.settingsCategoriesRelative(),
    )
}

fun NavHostController.navigateToClassificationRules() {
    navigateInGraph(
        graphRoute = Routes.SETTINGS_GRAPH,
        absoluteRoute = Routes.settingsClassificationRules(),
        relativeRoute = Routes.settingsClassificationRulesRelative(),
    )
}

fun NavHostController.navigateToClassificationLearned() {
    navigateInGraph(
        graphRoute = Routes.SETTINGS_GRAPH,
        absoluteRoute = Routes.settingsClassificationLearned(),
        relativeRoute = Routes.settingsClassificationLearnedRelative(),
    )
}

fun NavHostController.navigateUp(): Boolean = popBackStack()
