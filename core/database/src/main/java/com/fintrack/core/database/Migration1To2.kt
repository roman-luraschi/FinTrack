package com.fintrack.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("PRAGMA foreign_keys = OFF")

        db.execSQL(
            """
            ALTER TABLE accounts ADD COLUMN integrationProvider TEXT DEFAULT NULL
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE accounts ADD COLUMN externalAccountId TEXT DEFAULT NULL
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE accounts ADD COLUMN notificationListenerEnabled INTEGER NOT NULL DEFAULT 0
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS ingestion_batches (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                operationId TEXT NOT NULL,
                source TEXT NOT NULL,
                status TEXT NOT NULL,
                targetAccountId INTEGER,
                fileName TEXT,
                fileHash TEXT,
                recordCount INTEGER NOT NULL,
                insertedCount INTEGER NOT NULL,
                updatedCount INTEGER NOT NULL,
                skippedCount INTEGER NOT NULL,
                errorCount INTEGER NOT NULL,
                errorSummary TEXT,
                startedAt INTEGER NOT NULL,
                completedAt INTEGER,
                createdAt INTEGER NOT NULL,
                FOREIGN KEY(targetAccountId) REFERENCES accounts(id) ON UPDATE NO ACTION ON DELETE SET NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS index_ingestion_batches_operationId
            ON ingestion_batches (operationId)
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_ingestion_batches_source
            ON ingestion_batches (source)
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_ingestion_batches_status
            ON ingestion_batches (status)
            """.trimIndent(),
        )

        // SQLite cannot add FK constraints via ALTER TABLE — recreate transactions.
        db.execSQL(
            """
            CREATE TABLE transactions_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                externalId TEXT,
                amount TEXT NOT NULL,
                currency TEXT NOT NULL,
                type TEXT NOT NULL,
                status TEXT NOT NULL,
                description TEXT NOT NULL,
                descriptionRaw TEXT,
                merchantNormalized TEXT NOT NULL,
                categoryId INTEGER,
                subcategoryId INTEGER,
                classificationSource TEXT NOT NULL,
                classificationConfidence REAL,
                needsReview INTEGER NOT NULL,
                source TEXT NOT NULL,
                accountId INTEGER NOT NULL,
                transferAccountId INTEGER,
                transactionDate INTEGER NOT NULL,
                notes TEXT,
                ingestionBatchId INTEGER,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                deletedAt INTEGER,
                FOREIGN KEY(accountId) REFERENCES accounts(id) ON UPDATE NO ACTION ON DELETE RESTRICT,
                FOREIGN KEY(ingestionBatchId) REFERENCES ingestion_batches(id) ON UPDATE NO ACTION ON DELETE SET NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO transactions_new (
                id, externalId, amount, currency, type, status, description, descriptionRaw,
                merchantNormalized, categoryId, subcategoryId, classificationSource,
                classificationConfidence, needsReview, source, accountId, transferAccountId,
                transactionDate, notes, ingestionBatchId, createdAt, updatedAt, deletedAt
            )
            SELECT
                id, externalId, amount, 'ARS', type, 'CONFIRMED', description, NULL,
                merchantNormalized, categoryId, subcategoryId, classificationSource,
                classificationConfidence, needsReview, source, accountId, NULL,
                transactionDate, notes, NULL, createdAt, updatedAt, deletedAt
            FROM transactions
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE transactions")
        db.execSQL("ALTER TABLE transactions_new RENAME TO transactions")

        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_transactions_accountId_transactionDate
            ON transactions (accountId, transactionDate)
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_transactions_merchantNormalized
            ON transactions (merchantNormalized)
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_transactions_categoryId
            ON transactions (categoryId)
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_transactions_deletedAt
            ON transactions (deletedAt)
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_transactions_source_externalId
            ON transactions (source, externalId)
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_transactions_status
            ON transactions (status)
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_transactions_ingestionBatchId
            ON transactions (ingestionBatchId)
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS transaction_provenance (
                transactionId INTEGER NOT NULL PRIMARY KEY,
                integrationProvider TEXT NOT NULL,
                providerCode TEXT,
                rawPayload TEXT NOT NULL,
                payloadFormat TEXT NOT NULL,
                parseStatus TEXT NOT NULL,
                parserVersion TEXT NOT NULL,
                dedupMatchType TEXT NOT NULL,
                dedupMatchedTransactionId INTEGER,
                weakDedupKey TEXT,
                capturedAt INTEGER NOT NULL,
                metadataJson TEXT,
                FOREIGN KEY(transactionId) REFERENCES transactions(id)
                    ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS index_transaction_provenance_transactionId
            ON transaction_provenance (transactionId)
            """.trimIndent(),
        )

        db.execSQL("PRAGMA foreign_keys = ON")
    }
}
