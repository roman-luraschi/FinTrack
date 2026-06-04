package com.fintrack.core.data.dto

data class ManualTransactionDto(
    val amount: String,
    val type: String,
    val description: String,
    val accountId: Long,
    val transactionDate: String,
    val notes: String? = null,
    val categoryId: Long? = null,
    val transferAccountId: Long? = null,
    val currency: String = "ARS",
)
