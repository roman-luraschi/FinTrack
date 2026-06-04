package com.fintrack.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fintrack.core.domain.model.ClassificationSource
import com.fintrack.core.domain.model.TransactionSource
import com.fintrack.core.domain.model.TransactionStatus
import com.fintrack.core.domain.model.TransactionType
import java.math.BigDecimal
import java.time.Instant

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = IngestionBatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["ingestionBatchId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["accountId", "transactionDate"]),
        Index(value = ["merchantNormalized"]),
        Index(value = ["categoryId"]),
        Index(value = ["deletedAt"]),
        Index(value = ["source", "externalId"]),
        Index(value = ["status"]),
        Index(value = ["ingestionBatchId"]),
    ],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val externalId: String? = null,
    val amount: BigDecimal,
    val currency: String = "ARS",
    val type: TransactionType,
    val status: TransactionStatus,
    val description: String,
    val descriptionRaw: String? = null,
    val merchantNormalized: String,
    val categoryId: Long? = null,
    val subcategoryId: Long? = null,
    val classificationSource: ClassificationSource,
    val classificationConfidence: Float? = null,
    val needsReview: Boolean = false,
    val source: TransactionSource,
    val accountId: Long,
    val transferAccountId: Long? = null,
    val transactionDate: Instant,
    val notes: String? = null,
    val ingestionBatchId: Long? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant? = null,
)
