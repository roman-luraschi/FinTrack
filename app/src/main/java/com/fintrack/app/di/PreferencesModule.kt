package com.fintrack.app.di

import com.fintrack.app.data.device.DeviceIdentityPortAdapter
import com.fintrack.app.data.mercadopago.MercadoPagoConnectionPortAdapter
import com.fintrack.app.data.mercadopago.MercadoPagoSyncMetadataPortAdapter
import com.fintrack.app.data.preferences.UserSettingsPortAdapter
import com.fintrack.core.domain.repository.DeviceIdentityPort
import com.fintrack.core.domain.repository.MercadoPagoConnectionPort
import com.fintrack.core.domain.repository.MercadoPagoSyncMetadataPort
import com.fintrack.core.domain.repository.UserSettingsPort
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferencesModule {
    @Binds
    @Singleton
    abstract fun bindUserSettingsPort(adapter: UserSettingsPortAdapter): UserSettingsPort

    @Binds
    @Singleton
    abstract fun bindDeviceIdentityPort(adapter: DeviceIdentityPortAdapter): DeviceIdentityPort

    @Binds
    @Singleton
    abstract fun bindMercadoPagoConnectionPort(
        adapter: MercadoPagoConnectionPortAdapter,
    ): MercadoPagoConnectionPort

    @Binds
    @Singleton
    abstract fun bindMercadoPagoSyncMetadataPort(
        adapter: MercadoPagoSyncMetadataPortAdapter,
    ): MercadoPagoSyncMetadataPort
}
