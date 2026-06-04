package com.fintrack.core.domain.usecase.dashboard

import com.fintrack.core.domain.model.CategoryTotal
import com.fintrack.core.domain.model.DashboardOverview
import com.fintrack.core.domain.model.DashboardPeriod
import com.fintrack.core.domain.model.DashboardSummary
import com.fintrack.core.domain.model.Transaction
import com.fintrack.core.domain.model.TransactionFilter
import com.fintrack.core.domain.model.TransactionType
import com.fintrack.core.domain.repository.CategoryRepository
import com.fintrack.core.domain.repository.TransactionRepository
import com.fintrack.core.domain.repository.UserSettingsPort
import com.fintrack.core.domain.util.PeriodRangeResolver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import java.math.BigDecimal
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class ObserveDashboardSummaryUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val userSettingsPort: UserSettingsPort,
) {
    operator fun invoke(): Flow<DashboardSummary> =
        userSettingsPort.observeDashboardPeriod().flatMapLatest { period ->
            val now = Instant.now()
            val (start, end) = PeriodRangeResolver.resolve(period, now)
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

private const val RECENT_MOVEMENTS_LIMIT = 8

@Singleton
class ObserveDashboardOverviewUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
) {
    operator fun invoke(period: DashboardPeriod = DashboardPeriod.MONTH): Flow<DashboardOverview> =
        combine(
            transactionRepository.observeTransactions(TransactionFilter()),
            categoryRepository.observeCategories(),
        ) { transactions, categories ->
            val now = Instant.now()
            val (periodStart, periodEnd) = PeriodRangeResolver.resolve(period, now)
            val todayStart = PeriodRangeResolver.startOfDay(now)
            val todayEnd = PeriodRangeResolver.endOfDay(now)
            val categoryNames = categories.associateBy({ it.id }, { it.name })

            val inPeriod = transactions.filter { it.transactionDate.isInRange(periodStart, periodEnd) }
            val expensesInPeriod = inPeriod.filter { it.type == TransactionType.EXPENSE }
            val spentToday = transactions
                .filter { it.type == TransactionType.EXPENSE && it.transactionDate.isInRange(todayStart, todayEnd) }
                .fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.amount) }
            val spentThisMonth = expensesInPeriod
                .fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.amount) }

            val topExpenseCategory = expensesInPeriod
                .filter { it.categoryId != null }
                .groupBy { it.categoryId!! }
                .map { (categoryId, txs) ->
                    CategoryTotal(
                        categoryId = categoryId,
                        categoryName = categoryNames[categoryId] ?: "Sin nombre",
                        total = txs.fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.amount) },
                    )
                }
                .maxByOrNull { it.total }

            val recentTransactions = transactions
                .sortedWith(
                    compareByDescending<Transaction> { it.transactionDate }
                        .thenByDescending { it.id },
                )
                .take(RECENT_MOVEMENTS_LIMIT)

            DashboardOverview(
                spentToday = spentToday,
                spentThisMonth = spentThisMonth,
                movementCountInPeriod = inPeriod.size,
                topExpenseCategory = topExpenseCategory,
                recentTransactions = recentTransactions,
                categoryNamesById = categoryNames,
                periodStart = periodStart,
                periodEnd = periodEnd,
            )
        }
}

private fun Instant.isInRange(start: Instant, end: Instant): Boolean =
    !isBefore(start) && !isAfter(end)

@Singleton
class GetDashboardSummaryUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(period: DashboardPeriod): DashboardSummary {
        val now = Instant.now()
        val (start, end) = PeriodRangeResolver.resolve(period, now)
        return transactionRepository.getDashboardSummary(start, end)
    }
}
