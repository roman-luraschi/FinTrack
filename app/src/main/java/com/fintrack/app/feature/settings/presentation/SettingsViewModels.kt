package com.fintrack.app.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.app.data.preferences.UserPreferences
import com.fintrack.app.feature.mercadopago.MercadoPagoCustomTabLauncher
import com.fintrack.app.feature.mercadopago.MercadoPagoSyncScheduler
import com.fintrack.core.domain.model.BiometricAvailability
import com.fintrack.core.domain.model.MercadoPagoConnectionState
import com.fintrack.core.domain.repository.BiometricLockPort
import com.fintrack.core.domain.repository.NotificationAccessPort
import com.fintrack.core.domain.usecase.account.AddAccountUseCase
import com.fintrack.core.domain.usecase.account.DeleteAccountUseCase
import com.fintrack.core.domain.usecase.account.ObserveAccountsUseCase
import com.fintrack.core.domain.usecase.account.SetDefaultAccountUseCase
import com.fintrack.core.domain.usecase.account.UpdateAccountNotificationSettingsUseCase
import com.fintrack.core.domain.usecase.backend.EnsureDeviceRegisteredUseCase
import com.fintrack.core.domain.usecase.mercadopago.ConnectMercadoPagoUseCase
import com.fintrack.core.domain.usecase.mercadopago.DisconnectMercadoPagoUseCase
import com.fintrack.core.domain.usecase.mercadopago.ObserveMercadoPagoConnectionUseCase
import com.fintrack.core.domain.usecase.mercadopago.SyncMercadoPagoMovementsUseCase
import com.fintrack.core.domain.usecase.classification.AddClassificationRuleUseCase
import com.fintrack.core.domain.usecase.classification.DeleteClassificationRuleUseCase
import com.fintrack.core.domain.usecase.classification.ObserveClassificationRulesUseCase
import com.fintrack.core.domain.usecase.classification.ObserveLearnedMappingsUseCase
import com.fintrack.core.domain.usecase.classification.RevertLearnedMappingUseCase
import com.fintrack.core.domain.usecase.category.ObserveRootCategoriesUseCase
import com.fintrack.core.domain.common.DomainResult
import com.fintrack.core.domain.model.Account
import com.fintrack.core.domain.model.AccountType
import com.fintrack.core.domain.model.IntegrationProvider
import com.fintrack.core.domain.model.Category
import com.fintrack.core.domain.model.ClassificationRule
import com.fintrack.core.domain.model.DashboardPeriod
import com.fintrack.core.domain.model.LearnedMerchantCategory
import com.fintrack.core.domain.model.MatchType
import com.fintrack.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val accounts: List<Account> = emptyList(),
    val fuzzyThreshold: Float = UserPreferences.DEFAULT_FUZZY_THRESHOLD,
    val dashboardPeriod: DashboardPeriod = DashboardPeriod.MONTH,
    val biometricAvailability: BiometricAvailability = BiometricAvailability.Unavailable,
    val notificationListenerEnabled: Boolean = false,
    val movementAlertEnabled: Boolean = true,
    val showListenerDisconnectHint: Boolean = false,
    val mercadoPagoConnection: MercadoPagoConnectionState = MercadoPagoConnectionState(),
    val isConnectingMercadoPago: Boolean = false,
    val isSyncingMercadoPago: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null,
)

data class RulesUiState(
    val rules: List<ClassificationRule> = emptyList(),
    val categories: List<Category> = emptyList(),
    val message: String? = null,
    val errorMessage: String? = null,
)

data class LearnedUiState(
    val mappings: List<LearnedMerchantCategory> = emptyList(),
    val categories: List<Category> = emptyList(),
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeAccountsUseCase: ObserveAccountsUseCase,
    private val addAccountUseCase: AddAccountUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val setDefaultAccountUseCase: SetDefaultAccountUseCase,
    private val updateAccountNotificationSettingsUseCase: UpdateAccountNotificationSettingsUseCase,
    private val userPreferences: UserPreferences,
    private val biometricLockPort: BiometricLockPort,
    private val notificationAccessPort: NotificationAccessPort,
    private val ensureDeviceRegisteredUseCase: EnsureDeviceRegisteredUseCase,
    observeMercadoPagoConnectionUseCase: ObserveMercadoPagoConnectionUseCase,
    private val connectMercadoPagoUseCase: ConnectMercadoPagoUseCase,
    private val disconnectMercadoPagoUseCase: DisconnectMercadoPagoUseCase,
    private val syncMercadoPagoMovementsUseCase: SyncMercadoPagoMovementsUseCase,
    private val mercadoPagoCustomTabLauncher: MercadoPagoCustomTabLauncher,
    private val mercadoPagoSyncScheduler: MercadoPagoSyncScheduler,
) : ViewModel() {

    init {
        ensureBackendReady()
    }

    private val _message = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    private val _error = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    private val _biometricAvailability =
        kotlinx.coroutines.flow.MutableStateFlow(biometricLockPort.checkAvailability())
    private val _notificationListenerEnabled =
        kotlinx.coroutines.flow.MutableStateFlow(notificationAccessPort.isListenerEnabled())
    private val _isConnectingMercadoPago = kotlinx.coroutines.flow.MutableStateFlow(false)
    private val _isSyncingMercadoPago = kotlinx.coroutines.flow.MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> = combine(
        combine(
            combine(
                observeAccountsUseCase(),
                userPreferences.fuzzyThreshold,
                userPreferences.dashboardPeriod,
                userPreferences.movementAlertEnabled,
                _biometricAvailability,
            ) { accounts, threshold, period, movementAlerts, availability ->
                SettingsUiState(
                    accounts = accounts,
                    fuzzyThreshold = threshold,
                    dashboardPeriod = period,
                    movementAlertEnabled = movementAlerts,
                    biometricAvailability = availability,
                )
            },
            observeMercadoPagoConnectionUseCase(),
            _notificationListenerEnabled,
            _isConnectingMercadoPago,
            _isSyncingMercadoPago,
        ) { state, mpConnection, notifEnabled, isConnectingMp, isSyncingMp ->
            state.copy(
                mercadoPagoConnection = mpConnection,
                notificationListenerEnabled = notifEnabled,
                showListenerDisconnectHint = state.accounts.any { it.notificationListenerEnabled } && !notifEnabled,
                isConnectingMercadoPago = isConnectingMp,
                isSyncingMercadoPago = isSyncingMp,
            )
        },
        _message,
        _error,
    ) { state, message, error ->
        state.copy(
            message = message,
            errorMessage = error,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun refreshBiometricAvailability() {
        biometricLockPort.refreshLockState()
        _biometricAvailability.value = biometricLockPort.checkAvailability()
    }

    fun refreshNotificationAccess() {
        notificationAccessPort.refreshListenerState()
        _notificationListenerEnabled.value = notificationAccessPort.isListenerEnabled()
    }

    fun ensureBackendReady() {
        viewModelScope.launch {
            when (val result = ensureDeviceRegisteredUseCase(appVersion = BuildConfig.VERSION_NAME)) {
                is DomainResult.Success -> Unit
                is DomainResult.Error -> _error.value = result.message
            }
        }
    }

    fun openNotificationAccessSettings() {
        notificationAccessPort.openListenerSettings()
    }

    fun addAccount(name: String, type: AccountType) {
        viewModelScope.launch {
            when (val result = addAccountUseCase(name, type)) {
                is DomainResult.Success -> _message.value = "Cuenta creada"
                is DomainResult.Error -> _error.value = result.message
            }
        }
    }

    fun deleteAccount(id: Long) {
        viewModelScope.launch {
            deleteAccountUseCase(id)
            _message.value = "Cuenta eliminada"
        }
    }

    fun setDefaultAccount(id: Long) {
        viewModelScope.launch {
            setDefaultAccountUseCase(id)
            _message.value = "Cuenta predeterminada actualizada"
        }
    }

    fun setFuzzyThreshold(value: Float) {
        viewModelScope.launch {
            userPreferences.setFuzzyThreshold(value)
        }
    }

    fun setMovementAlertEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setMovementAlertEnabled(enabled)
        }
    }

    fun setAccountNotificationListener(accountId: Long, enabled: Boolean) {
        val account = uiState.value.accounts.firstOrNull { it.id == accountId } ?: return
        viewModelScope.launch {
            when (
                val result = updateAccountNotificationSettingsUseCase(
                    accountId = accountId,
                    notificationListenerEnabled = enabled,
                    integrationProvider = account.integrationProvider,
                )
            ) {
                is DomainResult.Success -> _message.value = "Configuración de notificaciones guardada"
                is DomainResult.Error -> _error.value = result.message
            }
        }
    }

    fun connectMercadoPago() {
        viewModelScope.launch {
            _isConnectingMercadoPago.value = true
            when (val result = connectMercadoPagoUseCase(appVersion = BuildConfig.VERSION_NAME)) {
                is DomainResult.Success -> mercadoPagoCustomTabLauncher.openAuthorizationUrl(result.data)
                is DomainResult.Error -> _error.value = result.message
            }
            _isConnectingMercadoPago.value = false
        }
    }

    fun disconnectMercadoPago() {
        viewModelScope.launch {
            disconnectMercadoPagoUseCase()
            mercadoPagoSyncScheduler.cancelAll()
            _message.value = "Mercado Pago desconectado"
        }
    }

    fun syncMercadoPagoMovements() {
        viewModelScope.launch {
            _isSyncingMercadoPago.value = true
            when (val result = syncMercadoPagoMovementsUseCase()) {
                is DomainResult.Success -> {
                    val sync = result.data
                    _message.value = when {
                        sync.fetched == 0 -> "No hay movimientos nuevos de Mercado Pago"
                        else -> "Mercado Pago: ${sync.inserted} nuevos, ${sync.updated} actualizados"
                    }
                }
                is DomainResult.Error -> _error.value = result.message
            }
            _isSyncingMercadoPago.value = false
        }
    }

    fun setAccountIntegrationProvider(accountId: Long, provider: IntegrationProvider) {
        val account = uiState.value.accounts.firstOrNull { it.id == accountId } ?: return
        viewModelScope.launch {
            when (
                val result = updateAccountNotificationSettingsUseCase(
                    accountId = accountId,
                    notificationListenerEnabled = account.notificationListenerEnabled,
                    integrationProvider = provider,
                )
            ) {
                is DomainResult.Success -> _message.value = "Configuración de notificaciones guardada"
                is DomainResult.Error -> _error.value = result.message
            }
        }
    }

}

@HiltViewModel
class ClassificationRulesViewModel @Inject constructor(
    observeRulesUseCase: ObserveClassificationRulesUseCase,
    observeRootCategoriesUseCase: ObserveRootCategoriesUseCase,
    private val addRuleUseCase: AddClassificationRuleUseCase,
    private val deleteRuleUseCase: DeleteClassificationRuleUseCase,
) : ViewModel() {

    val uiState: StateFlow<RulesUiState> = combine(
        observeRulesUseCase(),
        observeRootCategoriesUseCase(),
    ) { rules, categories ->
        RulesUiState(rules = rules, categories = categories)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RulesUiState())

    fun addRule(pattern: String, categoryId: Long) {
        viewModelScope.launch {
            addRuleUseCase(pattern, MatchType.CONTAINS, categoryId)
        }
    }

    fun deleteRule(id: Long) {
        viewModelScope.launch {
            deleteRuleUseCase(id)
        }
    }
}

@HiltViewModel
class LearnedMappingsViewModel @Inject constructor(
    observeLearnedUseCase: ObserveLearnedMappingsUseCase,
    observeRootCategoriesUseCase: ObserveRootCategoriesUseCase,
    private val revertUseCase: RevertLearnedMappingUseCase,
) : ViewModel() {

    val uiState: StateFlow<LearnedUiState> = combine(
        observeLearnedUseCase(),
        observeRootCategoriesUseCase(),
    ) { mappings, categories ->
        LearnedUiState(mappings = mappings, categories = categories)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LearnedUiState())

    fun revert(id: Long) {
        viewModelScope.launch {
            revertUseCase(id)
        }
    }
}
