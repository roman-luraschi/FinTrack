package com.fintrack.app.feature.settings.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fintrack.R
import com.fintrack.core.domain.model.Account
import com.fintrack.core.domain.model.IntegrationProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountNotificationSettingsRow(
    account: Account,
    onNotificationListenerChanged: (Boolean) -> Unit,
    onIntegrationProviderChanged: (IntegrationProvider) -> Unit,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var providerMenuExpanded by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth().padding(8.dp)) {
        Text(
            text = buildString {
                append(account.name)
                if (account.isDefault) append(" (default)")
            },
            style = MaterialTheme.typography.titleMedium,
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.account_notification_listener_title),
                    style = MaterialTheme.typography.bodyMedium,
                )
                val provider = account.integrationProvider
                if (account.notificationListenerEnabled && provider != null) {
                    Text(
                        text = provider.displayLabel(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Switch(
                checked = account.notificationListenerEnabled,
                onCheckedChange = onNotificationListenerChanged,
            )
        }

        ExposedDropdownMenuBox(
            expanded = providerMenuExpanded,
            onExpandedChange = { providerMenuExpanded = it },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) {
            OutlinedTextField(
                value = account.integrationProvider?.displayLabel()
                    ?: stringResource(R.string.account_integration_provider_none),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.account_integration_provider_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = providerMenuExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
            )
            DropdownMenu(
                expanded = providerMenuExpanded,
                onDismissRequest = { providerMenuExpanded = false },
            ) {
                selectableIntegrationProviders.forEach { provider ->
                    DropdownMenuItem(
                        text = { Text(provider.displayLabel()) },
                        onClick = {
                            providerMenuExpanded = false
                            onIntegrationProviderChanged(provider)
                        },
                    )
                }
            }
        }

        Row(modifier = Modifier.padding(top = 4.dp)) {
            TextButton(onClick = onSetDefault) {
                Text(stringResource(R.string.account_set_default))
            }
            if (!account.isDefault) {
                TextButton(onClick = onDelete) {
                    Text(stringResource(R.string.delete))
                }
            }
        }
    }
}
