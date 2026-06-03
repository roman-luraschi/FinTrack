package com.fintrack.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fintrack.core.database.entity.ClassificationRuleEntity
import com.fintrack.core.database.entity.LearnedMerchantCategoryEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface ClassificationDao {
    @Query("SELECT * FROM classification_rules ORDER BY priority DESC, pattern ASC")
    fun observeRules(): Flow<List<ClassificationRuleEntity>>

    @Query("SELECT * FROM classification_rules WHERE isActive = 1 ORDER BY priority DESC")
    suspend fun getActiveRules(): List<ClassificationRuleEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRule(rule: ClassificationRuleEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRules(rules: List<ClassificationRuleEntity>)

    @Update
    suspend fun updateRule(rule: ClassificationRuleEntity)

    @Query("DELETE FROM classification_rules WHERE id = :id")
    suspend fun deleteRule(id: Long)

    @Query(
        "SELECT * FROM learned_merchant_categories WHERE deletedAt IS NULL ORDER BY merchantNormalized ASC",
    )
    fun observeLearned(): Flow<List<LearnedMerchantCategoryEntity>>

    @Query(
        "SELECT * FROM learned_merchant_categories WHERE deletedAt IS NULL ORDER BY merchantNormalized ASC",
    )
    suspend fun getLearned(): List<LearnedMerchantCategoryEntity>

    @Query(
        "SELECT * FROM learned_merchant_categories WHERE merchantNormalized = :merchant AND deletedAt IS NULL LIMIT 1",
    )
    suspend fun getLearnedByMerchant(merchant: String): LearnedMerchantCategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLearned(mapping: LearnedMerchantCategoryEntity): Long

    @Query(
        "UPDATE learned_merchant_categories SET deletedAt = :deletedAt, updatedAt = :deletedAt WHERE id = :id",
    )
    suspend fun softDeleteLearned(id: Long, deletedAt: Instant)
}
