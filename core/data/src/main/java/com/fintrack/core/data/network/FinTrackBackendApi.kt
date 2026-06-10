package com.fintrack.core.data.network

import com.fintrack.core.data.network.dto.HealthResponseDto
import com.fintrack.core.data.network.dto.MercadoPagoMovementsResponseDto
import com.fintrack.core.data.network.dto.MercadoPagoOAuthStartRequestDto
import com.fintrack.core.data.network.dto.MercadoPagoOAuthStartResponseDto
import com.fintrack.core.data.network.dto.RegisterDeviceRequestDto
import com.fintrack.core.data.network.dto.RegisterDeviceResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface FinTrackBackendApi {
    @GET("api/health/")
    suspend fun health(): HealthResponseDto

    @POST("api/devices/register/")
    suspend fun registerDevice(
        @Body body: RegisterDeviceRequestDto,
    ): RegisterDeviceResponseDto

    @POST("api/oauth/mercadopago/start/")
    suspend fun startMercadoPagoOAuth(
        @Body body: MercadoPagoOAuthStartRequestDto,
    ): MercadoPagoOAuthStartResponseDto

    @GET("api/mercadopago/movements/")
    suspend fun fetchMercadoPagoMovements(
        @Query("device_id") deviceId: String,
        @Query("since") since: String? = null,
        @Query("limit") limit: Int = 50,
    ): MercadoPagoMovementsResponseDto
}
