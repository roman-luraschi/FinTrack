package com.fintrack.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        val now = System.currentTimeMillis()

        db.execSQL(
            """
            UPDATE transactions
            SET deletedAt = $now, updatedAt = $now
            WHERE deletedAt IS NULL
            AND externalId IS NOT NULL
            AND TRIM(externalId) != ''
            AND EXISTS (
                SELECT 1 FROM transactions keeper
                WHERE keeper.deletedAt IS NULL
                AND keeper.source = transactions.source
                AND keeper.externalId = transactions.externalId
                AND keeper.id < transactions.id
            )
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
