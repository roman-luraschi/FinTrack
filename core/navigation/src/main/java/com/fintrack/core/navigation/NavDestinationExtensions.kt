package com.fintrack.core.navigation

import androidx.navigation.NavDestination

private fun String.isMovementsListRoute(): Boolean =
    this == Routes.MOVEMENTS_LIST_BASE || this.startsWith("${Routes.MOVEMENTS_LIST_BASE}?")

private fun String.isMovementsDetailRoute(): Boolean =
    this == Routes.MOVEMENT_CREATE_BASE ||
        this.startsWith("${Routes.MOVEMENT_CREATE_BASE}/") ||
        this.startsWith("edit/")

private fun String.isSettingsDetailRoute(): Boolean =
    this == Routes.SETTINGS_CATEGORIES ||
        this == Routes.SETTINGS_CLASSIFICATION_RULES ||
        this == Routes.SETTINGS_CLASSIFICATION_LEARNED

fun NavDestination?.showsBottomBar(): Boolean {
    val route = this?.route ?: return false
    if (route == Routes.DASHBOARD) return true
    if (route.isMovementsDetailRoute() || route.isSettingsDetailRoute()) return false
    if (route.isMovementsListRoute()) return true
    if (route == Routes.SETTINGS_HOME) return true
    if (route == Routes.MOVEMENTS_GRAPH || route == Routes.SETTINGS_GRAPH) return true

    var parent = this.parent
    while (parent != null) {
        if (parent.route == Routes.MOVEMENTS_GRAPH && route.isMovementsListRoute()) return true
        if (parent.route == Routes.SETTINGS_GRAPH && route == Routes.SETTINGS_HOME) return true
        parent = parent.parent
    }
    return false
}

fun NavDestination?.isBottomNavTabSelected(tabRoute: String): Boolean {
    var current = this
    while (current != null) {
        if (current.route == tabRoute) return true
        current = current.parent
    }
    return false
}
