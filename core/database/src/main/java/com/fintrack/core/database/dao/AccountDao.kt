package com.fintrack.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fintrack.core.database.entity.AccountEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE deletedAt IS NULL ORDER BY isDefault DESC, name ASC")
    fun observeAll(): Flow<List<AccountEntity>>

    @Query(
        """
        SELECT * FROM accounts
        WHERE deletedAt IS NULL AND notificationListenerEnabled = 1
        ORDER BY isDefault DESC, name ASC
        """,
    )
    fun observeNotificationEnabled(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id AND deletedAt IS NULL")
    fun observeById(id: Long): Flow<AccountEntity?>

    @Query("SELECT * FROM accounts WHERE id = :id AND deletedAt IS NULL")
    suspend fun getById(id: Long): AccountEntity?

    @Query("SELECT * FROM accounts WHERE isDefault = 1 AND deletedAt IS NULL LIMIT 1")
    suspend fun getDefault(): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(account: AccountEntity): Long

    @Update
    suspend fun update(account: AccountEntity)

    @Query("UPDATE accounts SET deletedAt = :deletedAt, updatedAt = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: Long, deletedAt: Instant)

    @Query("UPDATE accounts SET isDefault = 0 WHERE deletedAt IS NULL")
    suspend fun clearDefaultFlags()

    @Query("UPDATE accounts SET isDefault = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setDefault(id: Long, updatedAt: Instant)
}
