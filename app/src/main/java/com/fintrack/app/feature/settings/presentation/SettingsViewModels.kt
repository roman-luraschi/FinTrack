package com.fintrack.app.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.app.data.preferences.UserPreferences
import com.fintrack.app.data.security.BiometricAuthResult
import com.fintrack.core.domain.model.BiometricAvailability
import com.fintrack.core.domain.repository.BiometricLockPort
import com.fintrack.core.domain.usecase.account.AddAccountUseCase
import com.fintrack.core.domain.usecase.account.DeleteAccountUseCase
import com.fintrack.core.domain.usecase.account.ObserveAccountsUseCase
import com.fintrack.core.domain.usecase.account.SetDefaultAccountUseCase
import com.fintrack.core.domain.usecase.classification.AddClassificationRuleUseCase
import com.fintrack.core.domain.usecase.classification.DeleteClassificationRuleUseCase
import com.fintrack.core.domain.usecase.classification.ObserveClassificationRulesUseCase
import com.fintrack.core.domain.usecase.classification.ObserveLearnedMappingsUseCase
import com.fintrack.core.domain.usecase.classification.RevertLearnedMappingUseCase
import com.fintrack.core.domain.usecase.category.ObserveRootCategoriesUseCase
import com.fintrack.core.domain.common.DomainResult
import com.fintrack.core.domain.model.Account
import com.fintrack.core.domain.model.AccountType
import com.fintrack.core.domain.model.Category
import com.fintrack.core.domain.model.ClassificationRule
import com.fintrack.core.domain.model.DashboardPeriod
import com.fintrack.core.domain.model.LearnedMerchantCategory
import com.fintrack.core.domain.model.MatchType
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
    val biometricLockEnabled: Boolean = false,
    val biometricAvailability: BiometricAvailability = BiometricAvailability.Unavailable,
    val requestEnableAuth: Boolean = false,
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
    private val userPreferences: UserPreferences,
    private val biometricLockPort: BiometricLockPort,
) : ViewModel() {

    private val _message = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    private val _error = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    private val _biometricAvailability =
        kotlinx.coroutines.flow.MutableStateFlow(biometricLockPort.checkAvailability())
    private val _requestEnableAuth = kotlinx.coroutines.flow.MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> = combine(
        observeAccountsUseCase(),
        userPreferences.fuzzyThreshold,
        userPreferences.dashboardPeriod,
        biometricLockPort.observeLockEnabled(),
        _biometricAvailability,
        _requestEnableAuth,
        _message,
        _error,
    ) { accounts, threshold, period, lockEnabled, availability, requestAuth, message, error ->
        SettingsUiState(
            accounts = accounts,
            fuzzyThreshold = threshold,
            dashboardPeriod = period,
            biometricLockEnabled = lockEnabled,
            biometricAvailability = availability,
            requestEnableAuth = requestAuth,
            message = message,
            errorMessage = error,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun refreshBiometricAvailability() {
        _biometricAvailability.value = biometricLockPort.checkAvailability()
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

    fun onBiometricLockToggle(enabled: Boolean) {
        refreshBiometricAvailability()
        if (!enabled) {
            viewModelScope.launch {
                biometricLockPort.setLockEnabled(false)
            }
            return
        }
        if (_biometricAvailability.value != BiometricAvailability.Ready) return
        _requestEnableAuth.value = true
    }

    fun onEnableAuthRequestConsumed() {
        _requestEnableAuth.value = false
    }

    fun onEnableAuthResult(result: BiometricAuthResult) {
        viewModelScope.launch {
            when (result) {
                is BiometricAuthResult.Success -> {
                    biometricLockPort.setLockEnabled(true)
                    _message.value = null
                }
                is BiometricAuthResult.Error -> {
                    biometricLockPort.setLockEnabled(false)
                }
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
