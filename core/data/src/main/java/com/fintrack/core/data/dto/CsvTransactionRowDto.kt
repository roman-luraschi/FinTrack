package com.fintrack.core.data.dto

data class CsvTransactionRowDto(
    val rowNumber: Int,
    val fileHash: String,
    val columnMappingId: String,
    val columns: Map<String, String>,
    val rawLine: String,
)
