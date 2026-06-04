package com.fintrack.core.domain.model

import java.math.BigDecimal
import java.time.Instant

data class DashboardOverview(
    val spentToday: BigDecimal,
    val spentThisMonth: BigDecimal,
    val movementCountInPeriod: Int,
    val topExpenseCategory: CategoryTotal?,
    val recentTransactions: List<Transaction>,
    val categoryNamesById: Map<Long, String>,
    val periodStart: Instant,
    val periodEnd: Instant,
)
