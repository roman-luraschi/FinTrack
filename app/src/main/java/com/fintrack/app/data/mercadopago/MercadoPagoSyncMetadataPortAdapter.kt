package com.fintrack.app.data.mercadopago

import com.fintrack.app.data.preferences.UserPreferences
import com.fintrack.core.domain.repository.MercadoPagoSyncMetadataPort
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MercadoPagoSyncMetadataPortAdapter @Inject constructor(
    private val userPreferences: UserPreferences,
) : MercadoPagoSyncMetadataPort {

    override suspend fun getLastSyncAt(): Instant? = userPreferences.getMercadoPagoLastSyncAt()

    override suspend fun setLastSyncAt(instant: Instant) {
        userPreferences.setMercadoPagoLastSyncAt(instant)
    }

    override suspend fun clearLastSyncAt() {
        userPreferences.clearMercadoPagoLastSyncAt()
    }
}
