package com.fintrack.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fintrack.core.domain.model.ChangeReason
import java.time.Instant

@Entity(
    tableName = "transaction_changes",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["transactionId"])],
)
data class TransactionChangeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionId: Long,
    val fieldName: String,
    val oldValue: String?,
    val newValue: String?,
    val changedAt: Instant,
    val changeReason: ChangeReason,
)
