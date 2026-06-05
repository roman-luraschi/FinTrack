package com.fintrack.core.navigation

object Routes {
    // Top-level (bottom navigation apunta al subgrafo)
    const val DASHBOARD = "dashboard"
    const val MOVEMENTS_GRAPH = "movements"
    const val SETTINGS_GRAPH = "settings"

    // Destinos relativos al subgrafo [MOVEMENTS_GRAPH]
    const val MOVEMENTS_LIST =
        "list?${NavArgs.ACCOUNT_ID}={${NavArgs.ACCOUNT_ID}}&${NavArgs.CATEGORY_ID}={${NavArgs.CATEGORY_ID}}"
    const val MOVEMENTS_LIST_BASE = "list"
    const val MOVEMENT_CREATE_BASE = "create"
    const val MOVEMENT_CREATE = "create/{${NavArgs.ACCOUNT_ID}}"
    const val MOVEMENT_EDIT = "edit/{${NavArgs.TRANSACTION_ID}}"

    // Destinos relativos al subgrafo [SETTINGS_GRAPH]
    const val SETTINGS_HOME = "home"
    const val SETTINGS_CATEGORIES = "categories"
    const val SETTINGS_CLASSIFICATION_RULES = "classification-rules"
    const val SETTINGS_CLASSIFICATION_LEARNED = "classification-learned"

    private fun movementsRoute(segment: String): String = "$MOVEMENTS_GRAPH/$segment"

    private fun settingsRoute(segment: String): String = "$SETTINGS_GRAPH/$segment"

    /** Ruta absoluta para [androidx.navigation.NavController.navigate] desde el NavHost raíz. */
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
        return movementsRoute(listSegment)
    }

    fun movementCreate(accountId: Long? = null): String =
        if (accountId == null) {
            movementsRoute(MOVEMENT_CREATE_BASE)
        } else {
            movementsRoute("$MOVEMENT_CREATE_BASE/$accountId")
        }

    fun movementEdit(transactionId: Long): String =
        movementsRoute("edit/$transactionId")

    fun settingsCategories(): String = settingsRoute(SETTINGS_CATEGORIES)

    fun settingsClassificationRules(): String = settingsRoute(SETTINGS_CLASSIFICATION_RULES)

    fun settingsClassificationLearned(): String = settingsRoute(SETTINGS_CLASSIFICATION_LEARNED)

    /** Rutas relativas al subgrafo actual (desde un destino ya dentro de movements/settings). */
    fun movementsListRelative(accountId: Long? = null, categoryId: Long? = null): String {
        val params = buildList {
            accountId?.let { add("${NavArgs.ACCOUNT_ID}=$it") }
            categoryId?.let { add("${NavArgs.CATEGORY_ID}=$it") }
        }
        return if (params.isEmpty()) {
            MOVEMENTS_LIST_BASE
        } else {
            "$MOVEMENTS_LIST_BASE?${params.joinToString("&")}"
        }
    }

    fun movementCreateRelative(accountId: Long? = null): String =
        if (accountId == null) {
            MOVEMENT_CREATE_BASE
        } else {
            "$MOVEMENT_CREATE_BASE/$accountId"
        }

    fun movementEditRelative(transactionId: Long): String = "edit/$transactionId"

    fun settingsCategoriesRelative(): String = SETTINGS_CATEGORIES

    fun settingsClassificationRulesRelative(): String = SETTINGS_CLASSIFICATION_RULES

    fun settingsClassificationLearnedRelative(): String = SETTINGS_CLASSIFICATION_LEARNED
}
