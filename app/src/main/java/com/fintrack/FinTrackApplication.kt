package com.fintrack

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.fintrack.BuildConfig
import com.fintrack.core.domain.common.DomainResult
import com.fintrack.app.feature.mercadopago.MercadoPagoOAuthCallbackHandler
import com.fintrack.app.feature.mercadopago.MercadoPagoSyncScheduler
import com.fintrack.core.domain.model.MercadoPagoConnectionStatus
import com.fintrack.core.domain.repository.MercadoPagoConnectionPort
import com.fintrack.core.domain.usecase.backend.EnsureDeviceRegisteredUseCase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class FinTrackApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var ensureDeviceRegisteredUseCase: EnsureDeviceRegisteredUseCase

    @Inject
    lateinit var mercadoPagoOAuthCallbackHandler: MercadoPagoOAuthCallbackHandler

    @Inject
    lateinit var mercadoPagoConnectionPort: MercadoPagoConnectionPort

    @Inject
    lateinit var mercadoPagoSyncScheduler: MercadoPagoSyncScheduler

    private val applicationScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        mercadoPagoOAuthCallbackHandler.start(applicationScope)
        applicationScope.launch {
            when (ensureDeviceRegisteredUseCase(appVersion = BuildConfig.VERSION_NAME)) {
                is DomainResult.Success -> Unit
                is DomainResult.Error -> Unit
            }
            val connection = mercadoPagoConnectionPort.observeConnection().first()
            if (connection.status == MercadoPagoConnectionStatus.CONNECTED) {
                mercadoPagoSyncScheduler.schedulePeriodic()
            }
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
