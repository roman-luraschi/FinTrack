package com.fintrack.core.domain.model

data class MercadoPagoSyncResult(
    val fetched: Int,
    val skipped: Int,
    val inserted: Int,
    val updated: Int,
    val ingestionSkipped: Int,
    val errors: Int,
    val parseErrors: List<String> = emptyList(),
)
