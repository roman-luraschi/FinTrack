package com.fintrack.core.navigation

sealed interface FinTrackDestination {
    data object Dashboard : FinTrackDestination

    data class MovementsList(
        val accountId: Long? = null,
        val categoryId: Long? = null,
    ) : FinTrackDestination

    data class MovementCreate(val accountId: Long? = null) : FinTrackDestination

    data class MovementEdit(val transactionId: Long) : FinTrackDestination

    data object Settings : FinTrackDestination

    data object Categories : FinTrackDestination

    data object ClassificationRules : FinTrackDestination

    data object ClassificationLearned : FinTrackDestination
}

fun FinTrackDestination.toRoute(): String = when (this) {
    FinTrackDestination.Dashboard -> Routes.DASHBOARD
    is FinTrackDestination.MovementsList -> Routes.movementsList(accountId, categoryId)
    is FinTrackDestination.MovementCreate -> Routes.movementCreate(accountId)
    is FinTrackDestination.MovementEdit -> Routes.movementEdit(transactionId)
    FinTrackDestination.Settings -> Routes.SETTINGS_GRAPH
    FinTrackDestination.Categories -> Routes.settingsCategories()
    FinTrackDestination.ClassificationRules -> Routes.settingsClassificationRules()
    FinTrackDestination.ClassificationLearned -> Routes.settingsClassificationLearned()
}
