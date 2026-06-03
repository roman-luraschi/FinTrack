package com.fintrack.core.navigation

object Routes {
    const val DASHBOARD = "dashboard"
    const val TRANSACTIONS = "transactions?accountId={accountId}&categoryId={categoryId}"
    const val TRANSACTIONS_BASE = "transactions"
    const val CATEGORIES = "categories"
    const val SETTINGS = "settings"
    const val TRANSACTION_ADD = "transaction/add?accountId={accountId}"
    const val TRANSACTION_DETAIL = "transaction/{id}"
    const val TRANSACTION_EDIT = "transaction/{id}/edit"
    const val ACCOUNT_ADD = "account/add"
    const val ACCOUNT_EDIT = "account/{id}/edit"
    const val CLASSIFICATION_RULES = "classification/rules"
    const val CLASSIFICATION_LEARNED = "classification/learned"

    fun transactions(accountId: Long? = null, categoryId: Long? = null): String {
        val params = buildList {
            accountId?.let { add("accountId=$it") }
            categoryId?.let { add("categoryId=$it") }
        }
        return if (params.isEmpty()) {
            TRANSACTIONS_BASE
        } else {
            "${TRANSACTIONS_BASE}?${params.joinToString("&")}"
        }
    }

    fun transactionAdd(accountId: Long? = null): String =
        "transaction/add?accountId=${accountId ?: -1L}"

    fun transactionDetail(id: Long): String = "transaction/$id"
    fun transactionEdit(id: Long): String = "transaction/$id/edit"
    fun accountEdit(id: Long): String = "account/$id/edit"
}
