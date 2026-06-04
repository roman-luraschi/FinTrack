package com.fintrack.core.navigation

object Routes {
    // Top-level (bottom navigation)
    const val DASHBOARD = "dashboard"
    const val MOVEMENTS_GRAPH = "movements"
    const val SETTINGS_GRAPH = "settings"

    // Nested: movements (relative to [MOVEMENTS_GRAPH])
    const val MOVEMENTS_LIST =
        "list?${NavArgs.ACCOUNT_ID}={${NavArgs.ACCOUNT_ID}}&${NavArgs.CATEGORY_ID}={${NavArgs.CATEGORY_ID}}"
    const val MOVEMENTS_LIST_BASE = "list"
    const val MOVEMENT_CREATE = "create?${NavArgs.ACCOUNT_ID}={${NavArgs.ACCOUNT_ID}}"
    const val MOVEMENT_EDIT = "edit/{${NavArgs.TRANSACTION_ID}}"

    // Nested: settings (relative to [SETTINGS_GRAPH])
    const val SETTINGS_HOME = "home"
    const val SETTINGS_CATEGORIES = "categories"
    const val SETTINGS_CLASSIFICATION_RULES = "classification-rules"
    const val SETTINGS_CLASSIFICATION_LEARNED = "classification-learned"

    // Future: account management, imports, movement detail
    // const val ACCOUNT_ADD = "account/add"
    // const val MOVEMENT_DETAIL = "detail/{transactionId}"

    fun movementsList(accountId: Long? = null, categoryId: Long? = null): String {
        val params = buildList {
            accountId?.let { add("${NavArgs.ACCOUNT_ID}=$it") }
            categoryId?.let { add("${NavArgs.CATEGORY_ID}=$it") }
        }
        val listSegment = if (params.isEmpty()) {
            MOVEMENTS_LIST_BASE
        } else {
            "$MOVEMENTS_LIST_BASE?${params.joinToString("&")}"
        }
        return "$MOVEMENTS_GRAPH/$listSegment"
    }

    fun movementCreate(accountId: Long? = null): String =
        "$MOVEMENTS_GRAPH/create?${NavArgs.ACCOUNT_ID}=${accountId ?: NavArgs.NONE}"

    fun movementEdit(transactionId: Long): String =
        "$MOVEMENTS_GRAPH/edit/$transactionId"

    fun settingsCategories(): String = "$SETTINGS_GRAPH/$SETTINGS_CATEGORIES"

    fun settingsClassificationRules(): String =
        "$SETTINGS_GRAPH/$SETTINGS_CLASSIFICATION_RULES"

    fun settingsClassificationLearned(): String =
        "$SETTINGS_GRAPH/$SETTINGS_CLASSIFICATION_LEARNED"
}
