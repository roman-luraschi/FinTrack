package com.fintrack.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.fintrack.core.database.converter.FinTrackTypeConverters
import com.fintrack.core.database.dao.AccountDao
import com.fintrack.core.database.dao.CategoryDao
import com.fintrack.core.database.dao.ClassificationDao
import com.fintrack.core.database.dao.IngestionBatchDao
import com.fintrack.core.database.dao.ProvenanceDao
import com.fintrack.core.database.dao.TransactionDao
import com.fintrack.core.database.entity.AccountEntity
import com.fintrack.core.database.entity.CategoryEntity
import com.fintrack.core.database.entity.ClassificationRuleEntity
import com.fintrack.core.database.entity.IngestionBatchEntity
import com.fintrack.core.database.entity.LearnedMerchantCategoryEntity
import com.fintrack.core.database.entity.TransactionChangeEntity
import com.fintrack.core.database.entity.TransactionEntity
import com.fintrack.core.database.entity.TransactionProvenanceEntity

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        IngestionBatchEntity::class,
        TransactionEntity::class,
        ClassificationRuleEntity::class,
        LearnedMerchantCategoryEntity::class,
        TransactionChangeEntity::class,
        TransactionProvenanceEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@TypeConverters(FinTrackTypeConverters::class)
abstract class FinTrackDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun classificationDao(): ClassificationDao
    abstract fun ingestionBatchDao(): IngestionBatchDao
    abstract fun provenanceDao(): ProvenanceDao
}
