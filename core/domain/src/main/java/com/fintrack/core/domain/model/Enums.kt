package com.fintrack.core.domain.model

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER,
}

enum class TransactionSource {
    MANUAL,
    MERCADO_PAGO_API,
    BANK_NOTIFICATION,
    CSV_IMPORT,
    XLSX_IMPORT,
    OCR,
}

enum class TransactionStatus {
    CONFIRMED,
    PENDING,
    NEEDS_REVIEW,
    DUPLICATE_CANDIDATE,
    REJECTED,
}

enum class IngestionBatchStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    PARTIAL,
    FAILED,
}

enum class DedupMatchType {
    NONE,
    STRONG,
    WEAK,
}

enum class ParseStatus {
    SUCCESS,
    PARTIAL,
    FAILED,
}

enum class IntegrationProvider {
    MERCADO_PAGO,
    GALICIA,
    SANTANDER,
    BBVA,
    MACRO,
    BRUBANK,
    UALA,
    PERSONAL_PAY,
    GENERIC_BANK,
    UNKNOWN,
}

enum class ClassificationSource {
    RULE,
    LEARNED,
    SIMILARITY,
    DEFAULT,
    USER_OVERRIDE,
}

enum class AccountType {
    CASH,
    BANK,
    DIGITAL_WALLET,
    CREDIT_CARD,
}

enum class MatchType {
    EXACT,
    PREFIX,
    CONTAINS,
    REGEX,
}

enum class ChangeReason {
    USER_EDIT,
    RECLASSIFICATION,
    IMPORT,
    SYNC,
    OCR_CORRECTION,
    NOTIFICATION_REPARSE,
}

enum class DashboardPeriod {
    WEEK,
    MONTH,
}
