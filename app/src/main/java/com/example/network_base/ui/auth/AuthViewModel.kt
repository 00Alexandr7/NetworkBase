package com.example.network_base.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.network_base.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state

    fun login(email: String, password: String, transfer: Boolean, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            _state.value = AuthUiState(loading = true)
            runCatching {
                repository.login(email.trim(), password, transfer)
            }.onSuccess {
                _state.value = AuthUiState(success = true)
                onDone(true)
            }.onFailure { e ->
                _state.value = AuthUiState(error = e.localizedMessage ?: "Ошибка входа")
                onDone(false)
            }
        }
    }

    fun register(name: String, email: String, password: String, transfer: Boolean, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            _state.value = AuthUiState(loading = true)
            runCatching {
                repository.register(name.trim(), email.trim(), password, transfer)
            }.onSuccess {
                _state.value = AuthUiState(success = true)
                onDone(true)
            }.onFailure { e ->
                _state.value = AuthUiState(error = e.localizedMessage ?: "Ошибка регистрации")
                onDone(false)
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun checkUserRole(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val isAdmin = repository.checkAdminRole()
            onResult(isAdmin)
        }
    }

    class Factory(
        private val repository: AuthRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                return AuthViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}


