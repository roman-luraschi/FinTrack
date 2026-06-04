package com.fintrack.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fintrack.core.domain.model.DedupMatchType
import com.fintrack.core.domain.model.IntegrationProvider
import com.fintrack.core.domain.model.ParseStatus
import java.time.Instant

@Entity(
    tableName = "transaction_provenance",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["transactionId"], unique = true)],
)
data class TransactionProvenanceEntity(
    @PrimaryKey val transactionId: Long,
    val integrationProvider: IntegrationProvider,
    val providerCode: String? = null,
    val rawPayload: String,
    val payloadFormat: String,
    val parseStatus: ParseStatus,
    val parserVersion: String,
    val dedupMatchType: DedupMatchType,
    val dedupMatchedTransactionId: Long? = null,
    val weakDedupKey: String? = null,
    val capturedAt: Instant,
    val metadataJson: String? = null,
)
