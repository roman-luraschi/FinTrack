package com.fintrack.core.navigation

import androidx.navigation.NavDestination

fun NavDestination?.showsBottomBar(): Boolean {
    var current = this
    while (current != null) {
        if (BottomNavItem.entries.any { it.route == current.route }) return true
        current = current.parent
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
