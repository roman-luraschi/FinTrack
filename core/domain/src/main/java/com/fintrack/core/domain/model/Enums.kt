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
}

enum class DashboardPeriod {
    WEEK,
    MONTH,
}
