package com.fintrack.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.fintrack.core.database.entity.CategoryTotalEntity
import com.fintrack.core.database.entity.TransactionChangeEntity
import com.fintrack.core.database.entity.TransactionEntity
import com.fintrack.core.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface TransactionDao {
    @Query(
        """
        SELECT * FROM transactions
        WHERE deletedAt IS NULL
        AND (:accountId IS NULL OR accountId = :accountId)
        AND (:categoryId IS NULL OR categoryId = :categoryId)
        AND (:type IS NULL OR type = :type)
        AND (:startDate IS NULL OR transactionDate >= :startDate)
        AND (:endDate IS NULL OR transactionDate <= :endDate)
        AND (
            :searchQuery IS NULL OR :searchQuery = '' OR
            merchantNormalized LIKE '%' || :searchQuery || '%' OR
            description LIKE '%' || :searchQuery || '%'
        )
        ORDER BY transactionDate DESC, id DESC
        """,
    )
    fun observeFiltered(
        accountId: Long?,
        categoryId: Long?,
        type: TransactionType?,
        startDate: Instant?,
        endDate: Instant?,
        searchQuery: String?,
    ): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id AND deletedAt IS NULL")
    fun observeById(id: Long): Flow<TransactionEntity?>

    @Query("SELECT * FROM transactions WHERE id = :id AND deletedAt IS NULL")
    suspend fun getById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertChanges(changes: List<TransactionChangeEntity>)

    @Query("UPDATE transactions SET deletedAt = :deletedAt, updatedAt = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: Long, deletedAt: Instant)

    @Query(
        """
        SELECT * FROM transactions
        WHERE deletedAt IS NULL
        AND amount = :amount
        AND merchantNormalized = :merchantNormalized
        AND transactionDate BETWEEN :startWindow AND :endWindow
        """,
    )
    suspend fun findDuplicatesInWindow(
        amount: java.math.BigDecimal,
        merchantNormalized: String,
        startWindow: Instant,
        endWindow: Instant,
    ): List<TransactionEntity>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE deletedAt IS NULL AND type = 'EXPENSE'
        AND transactionDate >= :startDate AND transactionDate <= :endDate
        """,
    )
    suspend fun sumExpenses(startDate: Instant, endDate: Instant): String

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE deletedAt IS NULL AND type = 'INCOME'
        AND transactionDate >= :startDate AND transactionDate <= :endDate
        """,
    )
    suspend fun sumIncome(startDate: Instant, endDate: Instant): String

    @Query(
        """
        SELECT t.categoryId AS categoryId, c.name AS categoryName, SUM(t.amount) AS total
        FROM transactions t
        LEFT JOIN categories c ON t.categoryId = c.id
        WHERE t.deletedAt IS NULL AND t.type = 'EXPENSE'
        AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate
        AND t.categoryId IS NOT NULL
        GROUP BY t.categoryId, c.name
        ORDER BY total DESC
        LIMIT :limit
        """,
    )
    suspend fun topExpenseCategories(
        startDate: Instant,
        endDate: Instant,
        limit: Int = 10,
    ): List<CategoryTotalEntity>

    @Transaction
    suspend fun updateWithChanges(
        transaction: TransactionEntity,
        changes: List<TransactionChangeEntity>,
    ) {
        update(transaction)
        if (changes.isNotEmpty()) {
            insertChanges(changes)
        }
    }
}
