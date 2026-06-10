package com.fintrack.app.feature.mercadopago

import android.net.Uri
import com.fintrack.core.domain.model.MercadoPagoOAuthCallback
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MercadoPagoOAuthDeepLinkDispatcher @Inject constructor() {

    private val _callbacks = MutableSharedFlow<MercadoPagoOAuthCallback>(extraBufferCapacity = 1)
    val callbacks: SharedFlow<MercadoPagoOAuthCallback> = _callbacks.asSharedFlow()

    fun dispatch(uri: Uri?) {
        val callback = MercadoPagoOAuthDeepLinkParser.parse(uri) ?: return
        _callbacks.tryEmit(callback)
    }
}
