package com.fintrack.app.feature.settings.presentation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.fintrack.R
import com.fintrack.core.designsystem.components.EmptyStateMessage
import com.fintrack.core.designsystem.components.ErrorMessage
import com.fintrack.app.feature.security.presentation.toHelpMessageRes
import com.fintrack.core.designsystem.components.FinTrackTopBar
import com.fintrack.core.domain.model.AccountType
import com.fintrack.core.domain.model.BiometricAvailability

@Composable
fun SettingsScreen(
    onNavigateToCategories: () -> Unit,
    onNavigateToRules: () -> Unit,
    onNavigateToLearned: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var accountName by rememberSaveable { mutableStateOf("") }

    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val postNotificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            viewModel.setMovementAlertEnabled(true)
        }
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.refreshBiometricAvailability()
            viewModel.refreshNotificationAccess()
            viewModel.ensureBackendReady()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        FinTrackTopBar(title = stringResource(R.string.nav_settings))
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.biometric_settings_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = when (uiState.biometricAvailability) {
                            BiometricAvailability.Ready ->
                                stringResource(R.string.biometric_settings_always_on)
                            else ->
                                stringResource(R.string.biometric_settings_unavailable)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    uiState.biometricAvailability.toHelpMessageRes()?.let { helpRes ->
                        Text(
                            text = stringResource(helpRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.notification_access_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = if (uiState.notificationListenerEnabled) {
                            stringResource(R.string.notification_access_enabled)
                        } else {
                            stringResource(R.string.notification_access_disabled)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Text(
                        text = stringResource(R.string.notification_access_privacy),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    if (uiState.showListenerDisconnectHint) {
                        Text(
                            text = stringResource(R.string.notification_access_oem_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    if (!uiState.notificationListenerEnabled) {
                        Button(
                            onClick = viewModel::openNotificationAccessSettings,
                            modifier = Modifier.padding(top = 8.dp),
                        ) {
                            Text(stringResource(R.string.notification_access_open_settings))
                        }
                    }
                }
            }
            item {
                MercadoPagoConnectionRow(
                    connection = uiState.mercadoPagoConnection,
                    isConnecting = uiState.isConnectingMercadoPago,
                    isSyncing = uiState.isSyncingMercadoPago,
                    onConnectClick = viewModel::connectMercadoPago,
                    onDisconnectClick = viewModel::disconnectMercadoPago,
                    onSyncClick = viewModel::syncMercadoPagoMovements,
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.movement_alert_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = stringResource(R.string.movement_alert_summary),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    Switch(
                        checked = uiState.movementAlertEnabled,
                        onCheckedChange = { enabled ->
                            if (!enabled) {
                                viewModel.setMovementAlertEnabled(false)
                                return@Switch
                            }
                            val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS,
                                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                            if (needsPermission) {
                                postNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.setMovementAlertEnabled(true)
                            }
                        },
                    )
                }
            }
            item {
                Text("Umbral fuzzy: ${"%.2f".format(uiState.fuzzyThreshold)}")
                Slider(
                    value = uiState.fuzzyThreshold,
                    onValueChange = viewModel::setFuzzyThreshold,
                    valueRange = 0.5f..1.0f,
                )
            }
            item {
                Text("Cuentas", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = accountName,
                    onValueChange = { accountName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre de cuenta") },
                )
                Button(
                    onClick = {
                        viewModel.addAccount(accountName, AccountType.BANK)
                        accountName = ""
                    },
                ) {
                    Text("Agregar cuenta")
                }
            }
            items(uiState.accounts, key = { it.id }) { account ->
                AccountNotificationSettingsRow(
                    account = account,
                    onNotificationListenerChanged = { enabled ->
                        viewModel.setAccountNotificationListener(account.id, enabled)
                    },
                    onIntegrationProviderChanged = { provider ->
                        viewModel.setAccountIntegrationProvider(account.id, provider)
                    },
                    onSetDefault = { viewModel.setDefaultAccount(account.id) },
                    onDelete = { viewModel.deleteAccount(account.id) },
                )
            }
            item {
                Text(
                    text = stringResource(R.string.nav_categories),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToCategories)
                        .padding(12.dp),
                )
            }
            item {
                Text(
                    text = "Reglas de clasificación",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToRules)
                        .padding(12.dp),
                )
            }
            item {
                Text(
                    text = "Aprendizajes del usuario",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToLearned)
                        .padding(12.dp),
                )
            }
            uiState.errorMessage?.let { item { ErrorMessage(it) } }
            uiState.message?.let { item { Text(it) } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassificationRulesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClassificationRulesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var pattern by rememberSaveable { mutableStateOf("") }
    var selectedCategoryId by rememberSaveable { mutableStateOf<Long?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        FinTrackTopBar(
            title = "Reglas",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            },
        )
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = pattern,
                onValueChange = { pattern = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Patrón") },
            )
            uiState.categories.take(5).forEach { category ->
                TextButton(onClick = { selectedCategoryId = category.id }) {
                    Text("${category.name}${if (selectedCategoryId == category.id) " ✓" else ""}")
                }
            }
            Button(
                onClick = {
                    selectedCategoryId?.let {
                        viewModel.addRule(pattern, it)
                        pattern = ""
                    }
                },
            ) { Text("Agregar regla") }
        }
        if (uiState.rules.isEmpty()) {
            EmptyStateMessage(message = stringResource(R.string.empty_rules))
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                items(uiState.rules, key = { it.id }) { rule ->
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("${rule.pattern} → cat ${rule.categoryId}")
                        TextButton(onClick = { viewModel.deleteRule(rule.id) }) {
                            Text(stringResource(R.string.delete))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnedMappingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LearnedMappingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val categoryMap = uiState.categories.associateBy { it.id }

    Column(modifier = modifier.fillMaxSize()) {
        FinTrackTopBar(
            title = "Aprendizajes",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            },
        )
        if (uiState.mappings.isEmpty()) {
            EmptyStateMessage(message = stringResource(R.string.empty_learned))
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                items(uiState.mappings, key = { it.id }) { mapping ->
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("${mapping.merchantNormalized} → ${categoryMap[mapping.categoryId]?.name ?: mapping.categoryId}")
                        TextButton(onClick = { viewModel.revert(mapping.id) }) {
                            Text("Revertir")
                        }
                    }
                }
            }
        }
    }
}
