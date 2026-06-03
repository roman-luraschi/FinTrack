package com.fintrack.app.data.repository

import com.fintrack.core.common.DispatcherProvider
import com.fintrack.core.database.dao.ClassificationDao
import com.fintrack.core.database.dao.TransactionDao
import com.fintrack.core.database.mapper.toBigDecimalSafe
import com.fintrack.core.database.mapper.toDomain
import com.fintrack.core.database.mapper.toEntity
import com.fintrack.core.domain.model.CategoryTotal
import com.fintrack.core.domain.model.DashboardSummary
import com.fintrack.core.domain.model.Transaction
import com.fintrack.core.domain.model.TransactionChange
import com.fintrack.core.domain.model.TransactionFilter
import com.fintrack.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val dispatchers: DispatcherProvider,
) : TransactionRepository {

    override fun observeTransactions(filter: TransactionFilter): Flow<List<Transaction>> =
        transactionDao.observeFiltered(
            accountId = filter.accountId,
            categoryId = filter.categoryId,
            type = filter.type,
            startDate = filter.startDate,
            endDate = filter.endDate,
            searchQuery = filter.searchQuery?.let { normalizeSearch(it) },
        ).map { list -> list.map { it.toDomain() } }

    override fun observeTransaction(id: Long): Flow<Transaction?> =
        transactionDao.observeById(id).map { it?.toDomain() }

    override suspend fun getTransaction(id: Long): Transaction? = withContext(dispatchers.io) {
        transactionDao.getById(id)?.toDomain()
    }

    override suspend fun insertTransaction(transaction: Transaction): Long = withContext(dispatchers.io) {
        transactionDao.insert(transaction.toEntity())
    }

    override suspend fun updateTransaction(
        transaction: Transaction,
        changes: List<TransactionChange>,
    ) = withContext(dispatchers.io) {
        transactionDao.updateWithChanges(
            transaction.toEntity(),
            changes.map { it.toEntity() },
        )
    }

    override suspend fun softDeleteTransaction(id: Long, deletedAt: Instant) = withContext(dispatchers.io) {
        transactionDao.softDelete(id, deletedAt)
    }

    override suspend fun findDuplicateCandidates(
        amount: BigDecimal,
        merchantNormalized: String,
        transactionDate: Instant,
        windowMinutes: Long,
    ): List<Transaction> = withContext(dispatchers.io) {
        val start = transactionDate.minus(windowMinutes, ChronoUnit.MINUTES)
        val end = transactionDate.plus(windowMinutes, ChronoUnit.MINUTES)
        transactionDao.findDuplicatesInWindow(amount, merchantNormalized, start, end)
            .map { it.toDomain() }
    }

    override suspend fun getDashboardSummary(
        startDate: Instant,
        endDate: Instant,
    ): DashboardSummary = withContext(dispatchers.io) {
        val expenses = transactionDao.sumExpenses(startDate, endDate).toBigDecimalSafe()
        val income = transactionDao.sumIncome(startDate, endDate).toBigDecimalSafe()
        val byCategory = transactionDao.topExpenseCategories(startDate, endDate)
            .map { it.toDomain() }

        DashboardSummary(
            totalExpenses = expenses,
            totalIncome = income,
            netBalance = income.subtract(expenses),
            byCategory = byCategory,
            periodStart = startDate,
            periodEnd = endDate,
        )
    }

    private fun normalizeSearch(query: String): String =
        query.trim().uppercase()
}
