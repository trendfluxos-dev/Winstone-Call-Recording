package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.WinstoneApp
import com.example.data.repository.AuthRepository
import com.example.model.UserRole
import com.example.model.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val session: UserSession) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository: AuthRepository = (application as WinstoneApp).authRepository
    private val repository = (application as WinstoneApp).repository

    val session: StateFlow<UserSession> = authRepository.session
    val crmBaseUrl: StateFlow<String> = authRepository.crmBaseUrl
    val sessionExpired: StateFlow<Boolean> = authRepository.sessionExpiredEvent

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String, employeeId: String, customBaseUrl: String? = null) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter both corporate email and password.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            customBaseUrl?.takeIf { it.isNotBlank() }?.let {
                authRepository.setBaseUrl(it)
            }

            val success = repository.login(email, password, employeeId)
            if (success) {
                _uiState.value = AuthUiState.Success(authRepository.session.value)
            } else {
                _uiState.value = AuthUiState.Error("Authentication failed. Please verify your Winstone CRM credentials.")
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState.Idle
    }

    fun updateServerUrl(url: String) {
        authRepository.setBaseUrl(url)
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
        authRepository.resetSessionExpiredEvent()
    }
}
