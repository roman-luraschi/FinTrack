package com.fintrack.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fintrack.core.database.entity.TransactionProvenanceEntity

@Dao
interface ProvenanceDao {
    @Query("SELECT * FROM transaction_provenance WHERE transactionId = :transactionId LIMIT 1")
    suspend fun getByTransactionId(transactionId: Long): TransactionProvenanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(provenance: TransactionProvenanceEntity)
}
