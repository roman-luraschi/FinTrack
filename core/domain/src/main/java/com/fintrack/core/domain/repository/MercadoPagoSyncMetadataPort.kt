package com.fintrack.core.domain.repository

import java.time.Instant

interface MercadoPagoSyncMetadataPort {
    suspend fun getLastSyncAt(): Instant?
    suspend fun setLastSyncAt(instant: Instant)
    suspend fun clearLastSyncAt()
}
