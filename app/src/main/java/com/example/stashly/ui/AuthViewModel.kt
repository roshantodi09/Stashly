package com.example.stashly.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.stashly.data.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(
    application: Application,
    private val authRepository: AuthRepository = AuthRepository()
) : AndroidViewModel(application) {

    val currentUser: StateFlow<FirebaseUser?> = authRepository.authStateFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = authRepository.currentUser
        )

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _isSignUpMode = MutableStateFlow(false)
    val isSignUpMode: StateFlow<Boolean> = _isSignUpMode.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun setEmail(value: String) { _email.value = value }
    fun setPassword(value: String) { _password.value = value }
    fun setConfirmPassword(value: String) { _confirmPassword.value = value }
    fun toggleAuthMode() {
        _isSignUpMode.value = !_isSignUpMode.value
        _errorMessage.value = null
    }
    fun clearError() { _errorMessage.value = null }

    fun submit(onSuccess: () -> Unit) {
        val emailVal = _email.value.trim()
        val passVal = _password.value.trim()

        if (emailVal.isBlank() || !emailVal.contains("@")) {
            _errorMessage.value = "Please enter a valid email address"
            return
        }
        if (passVal.length < 6) {
            _errorMessage.value = "Password must be at least 6 characters"
            return
        }

        if (_isSignUpMode.value) {
            val confirmVal = _confirmPassword.value.trim()
            if (passVal != confirmVal) {
                _errorMessage.value = "Passwords do not match"
                return
            }
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = if (_isSignUpMode.value) {
                authRepository.signUp(emailVal, passVal)
            } else {
                authRepository.signIn(emailVal, passVal)
            }

            _isLoading.value = false
            if (result.isSuccess) {
                onSuccess()
            } else {
                val exception = result.exceptionOrNull()
                val msg = exception?.localizedMessage ?: ""
                if (msg.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true)) {
                    _errorMessage.value = "Email/Password sign-in is not turned ON in Firebase Console. Please go to Firebase Console -> Build -> Authentication -> Sign-in method and enable Email/Password."
                } else {
                    _errorMessage.value = msg.ifBlank { "Authentication failed. Please check your credentials." }
                }
            }
        }
    }

    fun signInWithGoogle(idToken: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = authRepository.signInWithGoogleCredential(idToken)
            _isLoading.value = false
            if (result.isSuccess) {
                onSuccess()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.localizedMessage
                    ?: "Google Sign-In failed"
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                return AuthViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
