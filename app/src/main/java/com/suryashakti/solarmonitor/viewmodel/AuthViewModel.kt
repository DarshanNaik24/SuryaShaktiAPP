package com.suryashakti.solarmonitor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suryashakti.solarmonitor.data.User
import com.suryashakti.solarmonitor.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted // Added this import
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    val currentUser: StateFlow<User?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            val userId = authRepository.currentUserId.first()
            if (userId != null) {
                _authState.value = AuthState.Authenticated(userId)
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun login(userId: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.login(userId, pass)
            _authState.value = if (result.isSuccess) {
                AuthState.Authenticated(result.getOrThrow().userId)
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun signup(userId: String, pass: String, name: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signup(User(userId, pass, name))
            _authState.value = if (result.isSuccess) {
                AuthState.Authenticated(userId)
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Signup failed")
            }
        }
    }

    fun changePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading // Indicate a password change operation
            val userId = authRepository.currentUserId.first() // Get current user ID
            if (userId == null) {
                _authState.value = AuthState.Error("User not logged in.")
                return@launch
            }

            val result = authRepository.changePassword(userId, oldPassword, newPassword)
            _authState.value = if (result.isSuccess) {
                // Password changed successfully, re-authenticate or just update state
                // For simplicity, we can set it back to authenticated with current user ID
                AuthState.Authenticated(userId)
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Failed to change password")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun resetState() {
        if (_authState.value !is AuthState.Authenticated) {
            _authState.value = AuthState.Idle
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val userId: String) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}
