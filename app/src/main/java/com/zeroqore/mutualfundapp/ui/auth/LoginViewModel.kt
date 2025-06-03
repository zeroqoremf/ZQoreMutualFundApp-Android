// app/src/main/java/com/zeroqore/mutualfundapp/ui/auth/LoginViewModel.kt
package com.zeroqore.mutualfundapp.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zeroqore.mutualfundapp.data.auth.LoginRepository
import com.zeroqore.mutualfundapp.data.auth.LoginRequest
import com.zeroqore.mutualfundapp.data.auth.LoginResponse
import kotlinx.coroutines.launch

// Define a sealed class to represent the UI state of the login process
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val response: LoginResponse) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginUiState = MutableLiveData<LoginUiState>(LoginUiState.Idle)
    val loginUiState: LiveData<LoginUiState> = _loginUiState

    fun login(request: LoginRequest) {
        _loginUiState.value = LoginUiState.Loading // Set loading state

        viewModelScope.launch {
            val result = loginRepository.login(request)
            _loginUiState.value = if (result.isSuccess) {
                LoginUiState.Success(result.getOrThrow()) // Get successful response
            } else {
                LoginUiState.Error(result.exceptionOrNull()?.localizedMessage ?: "Unknown error")
            }
        }
    }

    // Call this to reset the state after handling success/error
    fun resetUiState() {
        _loginUiState.value = LoginUiState.Idle
    }

    /**
     * Factory for creating LoginViewModel with a constructor that takes LoginRepository.
     * Useful for ViewModel injection.
     */
    class Factory(private val loginRepository: LoginRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                return LoginViewModel(loginRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}