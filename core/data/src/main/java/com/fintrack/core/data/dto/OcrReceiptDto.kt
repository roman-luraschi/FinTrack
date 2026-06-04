package com.fintrack.core.data.dto

data class OcrReceiptDto(
    val sessionId: String,
    val rawText: String,
    val detectedAmount: String? = null,
    val detectedDate: String? = null,
    val detectedMerchant: String? = null,
    val confidenceOverall: Float? = null,
    val fieldConfidences: Map<String, Float>? = null,
    val imageUri: String? = null,
    val ocrEngine: String = "mlkit",
)
