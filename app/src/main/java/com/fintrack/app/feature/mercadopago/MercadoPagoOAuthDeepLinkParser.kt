package com.fintrack.app.feature.mercadopago

import android.net.Uri
import com.fintrack.core.domain.model.MercadoPagoOAuthCallback

object MercadoPagoOAuthDeepLinkParser {
    private const val SCHEME = "fintrack"
    private const val HOST = "oauth"
    private const val PATH = "/mercadopago"

    fun parse(uri: Uri?): MercadoPagoOAuthCallback? {
        if (uri == null) return null
        if (uri.scheme != SCHEME || uri.host != HOST) return null
        val path = uri.path ?: return null
        if (!path.startsWith(PATH)) return null

        val success = uri.getQueryParameter("success")
            ?.equals("true", ignoreCase = true)
            ?: false
        return MercadoPagoOAuthCallback(
            success = success,
            deviceId = uri.getQueryParameter("device_id"),
            reason = uri.getQueryParameter("reason"),
        )
    }
}
