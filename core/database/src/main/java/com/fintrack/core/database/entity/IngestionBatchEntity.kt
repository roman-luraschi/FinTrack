package com.fintrack.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fintrack.core.domain.model.IngestionBatchStatus
import com.fintrack.core.domain.model.TransactionSource
import java.time.Instant

@Entity(
    tableName = "ingestion_batches",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["targetAccountId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["operationId"], unique = true),
        Index(value = ["source"]),
        Index(value = ["status"]),
    ],
)
data class IngestionBatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val operationId: String,
    val source: TransactionSource,
    val status: IngestionBatchStatus,
    val targetAccountId: Long? = null,
    val fileName: String? = null,
    val fileHash: String? = null,
    val recordCount: Int = 0,
    val insertedCount: Int = 0,
    val updatedCount: Int = 0,
    val skippedCount: Int = 0,
    val errorCount: Int = 0,
    val errorSummary: String? = null,
    val startedAt: Instant,
    val completedAt: Instant? = null,
    val createdAt: Instant,
)
