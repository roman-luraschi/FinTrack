package com.fintrack.app.feature.dashboard.domain

import com.fintrack.core.common.DateUtils
import com.fintrack.core.domain.model.CategoryTotal
import com.fintrack.core.domain.model.DashboardPeriod
import com.fintrack.core.domain.model.DashboardSummary
import com.fintrack.core.domain.model.TransactionFilter
import com.fintrack.core.domain.model.TransactionType
import com.fintrack.core.domain.repository.CategoryRepository
import com.fintrack.core.domain.repository.TransactionRepository
import com.fintrack.app.data.preferences.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import java.math.BigDecimal
import java.time.Instant
import javax.inject.Inject

class ObserveDashboardSummaryUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val userPreferences: UserPreferences,
) {
    operator fun invoke(): Flow<DashboardSummary> =
        userPreferences.dashboardPeriod.flatMapLatest { period ->
            val now = Instant.now()
            val (start, end) = period.toRange(now)
            combine(
                transactionRepository.observeTransactions(
                    TransactionFilter(startDate = start, endDate = end),
                ),
                categoryRepository.observeCategories(),
            ) { transactions, categories ->
                val categoryNames = categories.associateBy({ it.id }, { it.name })
                val expenses = transactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.amount) }
                val income = transactions
                    .filter { it.type == TransactionType.INCOME }
                    .fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.amount) }

                val byCategory = transactions
                    .filter { it.type == TransactionType.EXPENSE && it.categoryId != null }
                    .groupBy { it.categoryId!! }
                    .map { (categoryId, txs) ->
                        CategoryTotal(
                            categoryId = categoryId,
                            categoryName = categoryNames[categoryId] ?: "Sin nombre",
                            total = txs.fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.amount) },
                        )
                    }
                    .sortedByDescending { it.total }
                    .take(10)

                DashboardSummary(
                    totalExpenses = expenses,
                    totalIncome = income,
                    netBalance = income.subtract(expenses),
                    byCategory = byCategory,
                    periodStart = start,
                    periodEnd = end,
                )
            }
        }
}

class GetDashboardSummaryUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(period: DashboardPeriod): DashboardSummary {
        val now = Instant.now()
        val (start, end) = period.toRange(now)
        return transactionRepository.getDashboardSummary(start, end)
    }
}

private fun DashboardPeriod.toRange(now: Instant): Pair<Instant, Instant> = when (this) {
    DashboardPeriod.WEEK -> DateUtils.startOfWeek(now) to DateUtils.endOfWeek(now)
    DashboardPeriod.MONTH -> DateUtils.startOfMonth(now) to DateUtils.endOfMonth(now)
}
