package com.fintrack.core.navigation

/**
 * Argument names shared by Navigation Compose routes and [androidx.lifecycle.SavedStateHandle].
 */
object NavArgs {
    const val TRANSACTION_ID = "transactionId"
    const val ACCOUNT_ID = "accountId"
    const val CATEGORY_ID = "categoryId"

    /** Sentinel for optional Long query/path arguments (no filter / no preselection). */
    const val NONE: Long = -1L
}
