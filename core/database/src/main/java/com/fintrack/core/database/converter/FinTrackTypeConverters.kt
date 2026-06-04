package com.fintrack.core.database.converter

import androidx.room.TypeConverter
import com.fintrack.core.domain.model.AccountType
import com.fintrack.core.domain.model.ChangeReason
import com.fintrack.core.domain.model.ClassificationSource
import com.fintrack.core.domain.model.DedupMatchType
import com.fintrack.core.domain.model.IngestionBatchStatus
import com.fintrack.core.domain.model.IntegrationProvider
import com.fintrack.core.domain.model.MatchType
import com.fintrack.core.domain.model.ParseStatus
import com.fintrack.core.domain.model.TransactionSource
import com.fintrack.core.domain.model.TransactionStatus
import com.fintrack.core.domain.model.TransactionType
import java.math.BigDecimal
import java.time.Instant

class FinTrackTypeConverters {
    @TypeConverter
    fun fromInstant(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun toInstant(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }

    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String? = value?.toPlainString()

    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal? = value?.let { BigDecimal(it) }

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun fromTransactionSource(value: TransactionSource): String = value.name

    @TypeConverter
    fun toTransactionSource(value: String): TransactionSource = TransactionSource.valueOf(value)

    @TypeConverter
    fun fromTransactionStatus(value: TransactionStatus): String = value.name

    @TypeConverter
    fun toTransactionStatus(value: String): TransactionStatus = TransactionStatus.valueOf(value)

    @TypeConverter
    fun fromIngestionBatchStatus(value: IngestionBatchStatus): String = value.name

    @TypeConverter
    fun toIngestionBatchStatus(value: String): IngestionBatchStatus =
        IngestionBatchStatus.valueOf(value)

    @TypeConverter
    fun fromDedupMatchType(value: DedupMatchType): String = value.name

    @TypeConverter
    fun toDedupMatchType(value: String): DedupMatchType = DedupMatchType.valueOf(value)

    @TypeConverter
    fun fromParseStatus(value: ParseStatus): String = value.name

    @TypeConverter
    fun toParseStatus(value: String): ParseStatus = ParseStatus.valueOf(value)

    @TypeConverter
    fun fromIntegrationProvider(value: IntegrationProvider?): String? = value?.name

    @TypeConverter
    fun toIntegrationProvider(value: String?): IntegrationProvider? =
        value?.let { IntegrationProvider.valueOf(it) }

    @TypeConverter
    fun fromClassificationSource(value: ClassificationSource): String = value.name

    @TypeConverter
    fun toClassificationSource(value: String): ClassificationSource =
        ClassificationSource.valueOf(value)

    @TypeConverter
    fun fromAccountType(value: AccountType): String = value.name

    @TypeConverter
    fun toAccountType(value: String): AccountType = AccountType.valueOf(value)

    @TypeConverter
    fun fromMatchType(value: MatchType): String = value.name

    @TypeConverter
    fun toMatchType(value: String): MatchType = MatchType.valueOf(value)

    @TypeConverter
    fun fromChangeReason(value: ChangeReason): String = value.name

    @TypeConverter
    fun toChangeReason(value: String): ChangeReason = ChangeReason.valueOf(value)
}
