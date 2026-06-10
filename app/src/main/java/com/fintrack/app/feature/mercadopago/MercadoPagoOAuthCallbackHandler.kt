package com.fintrack.app.feature.mercadopago

import com.fintrack.core.domain.common.DomainResult
import com.fintrack.core.domain.usecase.mercadopago.HandleMercadoPagoOAuthCallbackUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MercadoPagoOAuthCallbackHandler @Inject constructor(
    private val deepLinkDispatcher: MercadoPagoOAuthDeepLinkDispatcher,
    private val handleMercadoPagoOAuthCallbackUseCase: HandleMercadoPagoOAuthCallbackUseCase,
    private val mercadoPagoSyncScheduler: MercadoPagoSyncScheduler,
) {
    fun start(scope: CoroutineScope) {
        scope.launch {
            deepLinkDispatcher.callbacks.collect { callback ->
                when (handleMercadoPagoOAuthCallbackUseCase(callback)) {
                    is DomainResult.Success -> if (callback.success) {
                        mercadoPagoSyncScheduler.schedulePeriodic()
                        mercadoPagoSyncScheduler.scheduleImmediate()
                    }
                    is DomainResult.Error -> Unit
                }
            }
        }
    }
}
