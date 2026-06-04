package com.fintrack.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fintrack.core.database.MIGRATION_1_2
import com.fintrack.core.database.seed.DatabaseSeedData
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Instant
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FinTrackDatabase =
        Room.databaseBuilder(
            context,
            FinTrackDatabase::class.java,
            "fintrack.db",
        )
            .addMigrations(MIGRATION_1_2)
            .addCallback(SeedCallback())
            .build()

    @Provides
    @Singleton
    fun provideAccountDao(db: FinTrackDatabase) = db.accountDao()

    @Provides
    @Singleton
    fun provideCategoryDao(db: FinTrackDatabase) = db.categoryDao()

    @Provides
    @Singleton
    fun provideTransactionDao(db: FinTrackDatabase) = db.transactionDao()

    @Provides
    @Singleton
    fun provideClassificationDao(db: FinTrackDatabase) = db.classificationDao()

    @Provides
    @Singleton
    fun provideIngestionBatchDao(db: FinTrackDatabase) = db.ingestionBatchDao()

    @Provides
    @Singleton
    fun provideProvenanceDao(db: FinTrackDatabase) = db.provenanceDao()
}

private class SeedCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        // Room runs onCreate before returning; use direct SQL for seed on first creation
        val now = Instant.now().toEpochMilli()

        DatabaseSeedData.categories().forEach { category ->
            db.execSQL(
                """
                INSERT INTO categories (id, name, parentId, iconName, colorHex, isSystem, sortOrder, deletedAt)
                VALUES (${category.id}, '${category.name.replace("'", "''")}', NULL, NULL, NULL, 1, ${category.sortOrder}, NULL)
                """.trimIndent(),
            )
        }

        DatabaseSeedData.classificationRules().forEach { rule ->
            db.execSQL(
                """
                INSERT INTO classification_rules (pattern, matchType, categoryId, priority, isActive, createdAt)
                VALUES ('${rule.pattern.replace("'", "''")}', '${rule.matchType.name}', ${rule.categoryId}, ${rule.priority}, 1, $now)
                """.trimIndent(),
            )
        }

        db.execSQL(
            """
            INSERT INTO accounts (
                name, type, currency, colorHex, isDefault,
                integrationProvider, externalAccountId, notificationListenerEnabled,
                createdAt, updatedAt, deletedAt
            )
            VALUES ('Efectivo', 'CASH', 'ARS', '#1B5E20', 1, NULL, NULL, 0, $now, $now, NULL)
            """.trimIndent(),
        )
    }
}
