package com.fintrack.core.database.entity

import androidx.room.ColumnInfo
import java.math.BigDecimal

data class CategoryTotalEntity(
    @ColumnInfo(name = "categoryId") val categoryId: Long,
    @ColumnInfo(name = "categoryName") val categoryName: String,
    @ColumnInfo(name = "total") val total: BigDecimal,
)
