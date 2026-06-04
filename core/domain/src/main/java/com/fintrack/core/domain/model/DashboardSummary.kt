package com.fintrack.core.domain.model

import java.math.BigDecimal
import java.time.Instant

data class CategoryTotal(
    val categoryId: Long,
    val categoryName: String,
    val total: BigDecimal,
)

data class DashboardSummary(
    val totalExpenses: BigDecimal,
    val totalIncome: BigDecimal,
    val netBalance: BigDecimal,
    val byCategory: List<CategoryTotal>,
    val periodStart: Instant,
    val periodEnd: Instant,
)
