package com.fintrack.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fintrack.core.database.entity.IngestionBatchEntity

@Dao
interface IngestionBatchDao {
    @Query("SELECT * FROM ingestion_batches WHERE operationId = :operationId LIMIT 1")
    suspend fun getByOperationId(operationId: String): IngestionBatchEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(batch: IngestionBatchEntity): Long

    @Update
    suspend fun update(batch: IngestionBatchEntity)
}
