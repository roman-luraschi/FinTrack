package com.fintrack.app.feature.security.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.app.data.security.BiometricAuthError
import com.fintrack.app.data.security.BiometricAuthResult
import com.fintrack.core.domain.repository.BiometricLockPort
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppLockUiState(
    val lockEnabled: Boolean = false,
    val isLockStateReady: Boolean = false,
    val isUnlocked: Boolean = true,
    /** True after the user unlocked at least once this process (keeps NavHost mounted on re-lock). */
    val hasUnlockedOnce: Boolean = false,
    val authError: BiometricAuthError? = null,
    val requestAuth: Boolean = false,
)

@HiltViewModel
class AppLockViewModel @Inject constructor(
    private val biometricLockPort: BiometricLockPort,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppLockUiState())
    val uiState: StateFlow<AppLockUiState> = _uiState.asStateFlow()
    private var isAuthenticationInProgress = false

    init {
        viewModelScope.launch {
            val lockEnabledOnStart = biometricLockPort.observeLockEnabled().first()
            _uiState.update {
                it.copy(
                    lockEnabled = lockEnabledOnStart,
                    isLockStateReady = true,
                )
            }
            if (lockEnabledOnStart) {
                lock()
            }

            biometricLockPort.observeLockEnabled()
                .drop(1)
                .collect { enabled ->
                    val wasEnabled = _uiState.value.lockEnabled
                    _uiState.update { current ->
                        if (!enabled) {
                            current.copy(
                                lockEnabled = false,
                                isUnlocked = true,
                                requestAuth = false,
                                authError = null,
                            )
                        } else {
                            current.copy(lockEnabled = true)
                        }
                    }
                    if (enabled && !wasEnabled && _uiState.value.isUnlocked) {
                        lock()
                    }
                }
        }
    }

    fun onAppResumed() {
        biometricLockPort.refreshLockState()
    }

    fun onAuthenticationStarted() {
        isAuthenticationInProgress = true
    }

    fun onAuthenticationFinished() {
        isAuthenticationInProgress = false
    }

    fun lockWhenBackgrounded() {
        if (isAuthenticationInProgress) return
        val state = _uiState.value
        if (!state.lockEnabled || !state.isUnlocked) return
        lock()
    }

    fun lock() {
        _uiState.update {
            it.copy(
                isUnlocked = false,
                requestAuth = true,
                authError = null,
            )
        }
    }

    fun onLockScreenDisplayed() {
        val state = _uiState.value
        if (state.lockEnabled && !state.isUnlocked && !state.requestAuth) {
            retryAuth()
        }
    }

    fun retryAuth() {
        _uiState.update { it.copy(requestAuth = true, authError = null) }
    }

    fun onAuthRequestConsumed() {
        _uiState.update { it.copy(requestAuth = false) }
    }

    fun onAuthResult(result: BiometricAuthResult) {
        when (result) {
            is BiometricAuthResult.Success -> {
                _uiState.update {
                    it.copy(
                        isUnlocked = true,
                        hasUnlockedOnce = true,
                        requestAuth = false,
                        authError = null,
                    )
                }
            }
            is BiometricAuthResult.Error -> {
                _uiState.update {
                    it.copy(
                        requestAuth = false,
                        authError = result.type,
                    )
                }
            }
        }
    }

}
