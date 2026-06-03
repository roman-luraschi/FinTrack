package com.fintrack.app.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.app.data.preferences.UserPreferences
import com.fintrack.app.feature.accounts.domain.AddAccountUseCase
import com.fintrack.app.feature.accounts.domain.DeleteAccountUseCase
import com.fintrack.app.feature.accounts.domain.ObserveAccountsUseCase
import com.fintrack.app.feature.accounts.domain.SetDefaultAccountUseCase
import com.fintrack.app.feature.classification.domain.AddClassificationRuleUseCase
import com.fintrack.app.feature.classification.domain.DeleteClassificationRuleUseCase
import com.fintrack.app.feature.classification.domain.ObserveClassificationRulesUseCase
import com.fintrack.app.feature.classification.domain.ObserveLearnedMappingsUseCase
import com.fintrack.app.feature.classification.domain.RevertLearnedMappingUseCase
import com.fintrack.app.feature.categories.domain.ObserveRootCategoriesUseCase
import com.fintrack.core.common.Result
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
) : ViewModel() {

    private val _message = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    private val _error = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        observeAccountsUseCase(),
        userPreferences.fuzzyThreshold,
        userPreferences.dashboardPeriod,
        _message,
        _error,
    ) { accounts, threshold, period, message, error ->
        SettingsUiState(
            accounts = accounts,
            fuzzyThreshold = threshold,
            dashboardPeriod = period,
            message = message,
            errorMessage = error,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun addAccount(name: String, type: AccountType) {
        viewModelScope.launch {
            when (val result = addAccountUseCase(name, type)) {
                is Result.Success -> _message.value = "Cuenta creada"
                is Result.Error -> _error.value = result.message
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
