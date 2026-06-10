package com.fintrack.core.data.network.dto

import com.squareup.moshi.Json

data class HealthResponseDto(
    val status: String,
    val service: String? = null,
)

data class RegisterDeviceRequestDto(
    @Json(name = "device_id") val deviceId: String,
    @Json(name = "fcm_token") val fcmToken: String = "",
    @Json(name = "app_version") val appVersion: String = "",
)

data class RegisterDeviceResponseDto(
    @Json(name = "device_id") val deviceId: String,
    @Json(name = "fcm_token") val fcmToken: String? = null,
    @Json(name = "app_version") val appVersion: String? = null,
)

data class MercadoPagoOAuthStartRequestDto(
    @Json(name = "device_id") val deviceId: String,
)

data class MercadoPagoOAuthStartResponseDto(
    @Json(name = "authorization_url") val authorizationUrl: String,
)
