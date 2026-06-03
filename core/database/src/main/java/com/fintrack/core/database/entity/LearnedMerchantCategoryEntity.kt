package com.fintrack.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "learned_merchant_categories",
    indices = [Index(value = ["merchantNormalized"], unique = true)],
)
data class LearnedMerchantCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val merchantNormalized: String,
    val categoryId: Long,
    val subcategoryId: Long? = null,
    val learnedAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant? = null,
)
