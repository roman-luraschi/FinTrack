package com.fintrack.core.data.mercadopago

import com.fintrack.core.common.DispatcherProvider
import com.fintrack.core.data.mapper.toDraft
import com.fintrack.core.data.network.FinTrackBackendApi
import com.fintrack.core.domain.common.DomainResult
import com.fintrack.core.domain.model.TransactionDraft
import com.fintrack.core.domain.repository.MercadoPagoFetchResult
import com.fintrack.core.domain.repository.MercadoPagoSyncPort
import com.squareup.moshi.Moshi
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MercadoPagoSyncPortImpl @Inject constructor(
    private val api: FinTrackBackendApi,
    private val moshi: Moshi,
    private val dispatchers: DispatcherProvider,
) : MercadoPagoSyncPort {

    override suspend fun fetchMovementDrafts(
        deviceId: String,
        accountId: Long,
        since: Instant?,
        limit: Int,
    ): DomainResult<MercadoPagoFetchResult> = withContext(dispatchers.io) {
        try {
            val response = api.fetchMercadoPagoMovements(
                deviceId = deviceId,
                since = since?.toMercadoPagoSinceParam(),
                limit = limit,
            )
            val mapped = MercadoPagoMovementsMapper.map(response, moshi)
            val drafts = mutableListOf<TransactionDraft>()
            val draftErrors = mapped.parseErrors.toMutableList()

            for (dto in mapped.transactions) {
                when (val draftResult = dto.toDraft(accountId)) {
                    is DomainResult.Success -> drafts.add(draftResult.data)
                    is DomainResult.Error -> draftErrors.add(draftResult.message)
                }
            }

            DomainResult.Success(
                MercadoPagoFetchResult(
                    drafts = drafts,
                    skipped = mapped.skipped,
                    parseErrors = draftErrors,
                ),
            )
        } catch (error: Exception) {
            DomainResult.Error(mapNetworkError(error), error)
        }
    }

    private fun mapNetworkError(error: Exception): String = when (error) {
        is HttpException -> when (error.code()) {
            502 -> "No se pudieron obtener movimientos de Mercado Pago"
            401, 403 -> "Mercado Pago no está autorizado. Reconectá la cuenta."
            else -> "Error del backend (${error.code()})"
        }
        is IOException -> "Sin conexión al backend"
        else -> error.message ?: "Error de red"
    }

    private fun Instant.toMercadoPagoSinceParam(): String =
        atZone(ZoneId.of("America/Argentina/Buenos_Aires"))
            .format(MP_SINCE_FORMATTER)

    private companion object {
        val MP_SINCE_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    }
}
