package com.fintrack.app.feature.settings.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fintrack.R
import com.fintrack.core.domain.model.MercadoPagoConnectionState
import com.fintrack.core.domain.model.MercadoPagoConnectionStatus

@Composable
fun MercadoPagoConnectionRow(
    connection: MercadoPagoConnectionState,
    isConnecting: Boolean,
    isSyncing: Boolean,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    onSyncClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.mercadopago_connection_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = when (connection.status) {
                MercadoPagoConnectionStatus.CONNECTED ->
                    stringResource(R.string.mercadopago_connection_connected)
                MercadoPagoConnectionStatus.ERROR ->
                    connection.errorMessage
                        ?: stringResource(R.string.mercadopago_connection_error)
                MercadoPagoConnectionStatus.DISCONNECTED ->
                    stringResource(R.string.mercadopago_connection_disconnected)
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (connection.status == MercadoPagoConnectionStatus.ERROR) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            text = stringResource(R.string.mercadopago_connection_privacy),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
        if (connection.status == MercadoPagoConnectionStatus.CONNECTED) {
            Button(
                onClick = onSyncClick,
                enabled = !isSyncing,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text(
                    if (isSyncing) {
                        stringResource(R.string.mercadopago_sync_in_progress)
                    } else {
                        stringResource(R.string.mercadopago_sync_now)
                    },
                )
            }
            TextButton(
                onClick = onDisconnectClick,
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Text(stringResource(R.string.mercadopago_connection_disconnect))
            }
        } else {
            Button(
                onClick = onConnectClick,
                enabled = !isConnecting,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text(
                    if (isConnecting) {
                        stringResource(R.string.mercadopago_connection_connecting)
                    } else {
                        stringResource(R.string.mercadopago_connection_connect)
                    },
                )
            }
        }
    }
}
