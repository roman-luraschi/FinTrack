package com.fintrack.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Restores the non-unique index Room expects on [TransactionEntity],
 * for devices that ran an earlier 2→3 migration that dropped it.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_transactions_source_externalId
            ON transactions (source, externalId)
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS index_transactions_source_externalId_active
            ON transactions (source, externalId)
            WHERE externalId IS NOT NULL AND deletedAt IS NULL
            """.trimIndent(),
        )
    }
}
