package com.fintrack.core.navigation

import androidx.navigation.NavHostController

fun NavHostController.navigateToDashboard() {
    navigate(FinTrackDestination.Dashboard.toRoute())
}

fun NavHostController.navigateToMovementsList(
    accountId: Long? = null,
    categoryId: Long? = null,
) {
    navigate(FinTrackDestination.MovementsList(accountId, categoryId).toRoute())
}

fun NavHostController.navigateToCreateMovement(accountId: Long? = null) {
    navigate(FinTrackDestination.MovementCreate(accountId).toRoute())
}

fun NavHostController.navigateToEditMovement(transactionId: Long) {
    navigate(FinTrackDestination.MovementEdit(transactionId).toRoute())
}

fun NavHostController.navigateToSettings() {
    navigate(FinTrackDestination.Settings.toRoute())
}

fun NavHostController.navigateToCategories() {
    navigate(FinTrackDestination.Categories.toRoute())
}

fun NavHostController.navigateToClassificationRules() {
    navigate(FinTrackDestination.ClassificationRules.toRoute())
}

fun NavHostController.navigateToClassificationLearned() {
    navigate(FinTrackDestination.ClassificationLearned.toRoute())
}

fun NavHostController.navigateUp(): Boolean = popBackStack()
