package com.fintrack.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fintrack.core.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE deletedAt IS NULL ORDER BY sortOrder ASC, name ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query(
        "SELECT * FROM categories WHERE parentId IS NULL AND deletedAt IS NULL ORDER BY sortOrder ASC, name ASC",
    )
    fun observeRootCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id AND deletedAt IS NULL")
    suspend fun getById(id: Long): CategoryEntity?

    @Query("SELECT * FROM categories WHERE id IN (:ids) AND deletedAt IS NULL")
    suspend fun getByIds(ids: List<Long>): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(categories: List<CategoryEntity>): List<Long>

    @Update
    suspend fun update(category: CategoryEntity)

    @Query("UPDATE categories SET deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: Long, deletedAt: Instant)

    @Query(
        "SELECT COUNT(*) FROM transactions WHERE categoryId = :categoryId AND deletedAt IS NULL",
    )
    suspend fun countTransactionsUsingCategory(categoryId: Long): Int
}
