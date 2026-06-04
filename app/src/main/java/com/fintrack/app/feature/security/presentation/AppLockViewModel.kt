package com.fintrack.app.feature.security.presentation

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.app.data.security.BiometricAuthError
import com.fintrack.app.data.security.BiometricAuthResult
import com.fintrack.core.domain.repository.BiometricLockPort
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppLockUiState(
    val lockEnabled: Boolean = false,
    val isUnlocked: Boolean = true,
    val showLockOverlay: Boolean = false,
    val authError: BiometricAuthError? = null,
    val requestAuth: Boolean = false,
)

@HiltViewModel
class AppLockViewModel @Inject constructor(
    private val biometricLockPort: BiometricLockPort,
) : ViewModel(), DefaultLifecycleObserver {

    private val _uiState = MutableStateFlow(AppLockUiState())
    val uiState: StateFlow<AppLockUiState> = _uiState.asStateFlow()
    private var isFirstLockPreferenceEmission = true

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        viewModelScope.launch {
            biometricLockPort.observeLockEnabled().collect { enabled ->
                val wasEnabled = _uiState.value.lockEnabled
                _uiState.update { current ->
                    if (!enabled) {
                        current.copy(
                            lockEnabled = false,
                            isUnlocked = true,
                            showLockOverlay = false,
                            requestAuth = false,
                            authError = null,
                        )
                    } else {
                        current.copy(lockEnabled = true)
                    }
                }
                when {
                    isFirstLockPreferenceEmission -> {
                        isFirstLockPreferenceEmission = false
                        if (enabled) lock()
                    }
                    enabled && !wasEnabled -> Unit
                }
            }
        }
    }

    fun onColdStart() {
        if (_uiState.value.lockEnabled && !_uiState.value.showLockOverlay) {
            lock()
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        val state = _uiState.value
        if (state.lockEnabled && state.isUnlocked) {
            lock()
        }
    }

    fun lock() {
        _uiState.update {
            it.copy(
                isUnlocked = false,
                showLockOverlay = true,
                requestAuth = true,
                authError = null,
            )
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
                        showLockOverlay = false,
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

    override fun onCleared() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        super.onCleared()
    }
}
