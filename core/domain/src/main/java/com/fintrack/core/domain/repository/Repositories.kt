package com.fintrack.core.domain.repository

import com.fintrack.core.domain.model.Account
import com.fintrack.core.domain.model.Category
import com.fintrack.core.domain.model.ClassificationRule
import com.fintrack.core.domain.model.DashboardSummary
import com.fintrack.core.domain.model.LearnedMerchantCategory
import com.fintrack.core.domain.model.Transaction
import com.fintrack.core.domain.model.TransactionChange
import com.fintrack.core.domain.model.TransactionFilter
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface AccountRepository {
    fun observeAccounts(): Flow<List<Account>>
    fun observeNotificationEnabledAccounts(): Flow<List<Account>>
    fun observeAccount(id: Long): Flow<Account?>
    suspend fun getAccount(id: Long): Account?
    suspend fun getDefaultAccount(): Account?
    suspend fun insertAccount(account: Account): Long
    suspend fun updateAccount(account: Account)
    suspend fun softDeleteAccount(id: Long, deletedAt: Instant)
    suspend fun setDefaultAccount(id: Long)
}

interface CategoryRepository {
    fun observeCategories(): Flow<List<Category>>
    fun observeRootCategories(): Flow<List<Category>>
    suspend fun getCategory(id: Long): Category?
    suspend fun getCategoriesByIds(ids: List<Long>): List<Category>
    suspend fun insertCategory(category: Category): Long
    suspend fun updateCategory(category: Category)
    suspend fun softDeleteCategory(id: Long, deletedAt: Instant)
    suspend fun countTransactionsUsingCategory(categoryId: Long): Int
}

interface TransactionRepository {
    fun observeTransactions(filter: TransactionFilter): Flow<List<Transaction>>
    fun observeTransaction(id: Long): Flow<Transaction?>
    suspend fun getTransaction(id: Long): Transaction?
    suspend fun insertTransaction(transaction: Transaction): Long
    suspend fun updateTransaction(
        transaction: Transaction,
        changes: List<TransactionChange>,
    )
    suspend fun softDeleteTransaction(id: Long, deletedAt: Instant)
    suspend fun findDuplicateCandidates(
        amount: java.math.BigDecimal,
        merchantNormalized: String,
        transactionDate: Instant,
        windowMinutes: Long = 2,
    ): List<Transaction>
    suspend fun getDashboardSummary(startDate: Instant, endDate: Instant): DashboardSummary
}

interface ClassificationRepository {
    fun observeRules(): Flow<List<ClassificationRule>>
    fun observeLearnedMappings(): Flow<List<LearnedMerchantCategory>>
    suspend fun getActiveRules(): List<ClassificationRule>
    suspend fun getLearnedMappings(): List<LearnedMerchantCategory>
    suspend fun insertRule(rule: ClassificationRule): Long
    suspend fun updateRule(rule: ClassificationRule)
    suspend fun deleteRule(id: Long)
    suspend fun upsertLearnedMapping(mapping: LearnedMerchantCategory): Long
    suspend fun softDeleteLearnedMapping(id: Long, deletedAt: Instant)
}
