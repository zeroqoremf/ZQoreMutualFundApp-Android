// app/src/main/java/com/zeroqore/mutualfundapp/ui/login/LoginViewModel.kt
package com.zeroqore.mutualfundapp.ui.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zeroqore.mutualfundapp.data.AuthTokenManager
import com.zeroqore.mutualfundapp.data.auth.LoginRepository
import com.zeroqore.mutualfundapp.data.auth.LoginRequest
import com.zeroqore.mutualfundapp.data.auth.LoginResponse
import com.zeroqore.mutualfundapp.util.Results // CORRECTED: Importing YOUR custom Results sealed class
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginRepository: LoginRepository,
    private val authTokenManager: AuthTokenManager
) : ViewModel() {

    // CORRECTED: _loginResult now uses YOUR custom Results sealed class
    private val _loginResult = MutableLiveData<Results<LoginResponse>>()
    val loginResult: LiveData<Results<LoginResponse>> = _loginResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun login(username: String, password: String) {
        _isLoading.value = true // Show loading indicator
        _loginResult.value = Results.Loading // Set initial loading state in YOUR custom Results

        viewModelScope.launch {
            val request = LoginRequest(username, password)
            // loginRepository.login returns kotlin.Result<LoginResponse>
            val repoResult: kotlin.Result<LoginResponse> = loginRepository.login(request)

            // CORRECTED: Mapping kotlin.Result to YOUR custom Results sealed class
            repoResult
                .onSuccess { loginResponse ->
                    // SAVE AUTH DATA HERE if the repository call was successful
                    authTokenManager.saveAuthData(
                        accessToken = loginResponse.accessToken,
                        refreshToken = loginResponse.refreshToken,
                        expiresIn = loginResponse.expiresIn,
                        tokenType = loginResponse.tokenType,
                        investorId = loginResponse.investorId,
                        distributorId = loginResponse.distributorId,
                        investorName = loginResponse.investorName
                    )
                    Log.d("LoginViewModel", "Auth data saved to SharedPreferences.")
                    // Update LiveData with YOUR custom Results.Success
                    _loginResult.value = Results.Success(loginResponse)
                }
                .onFailure { exception ->
                    // Update LiveData with YOUR custom Results.Error
                    Log.e("LoginViewModel", "Login failed: ${exception.message}", exception)
                    _loginResult.value = Results.Error(exception, exception.message)
                }

            _isLoading.value = false // Hide loading indicator regardless of success or failure
        }
    }

    /**
     * Factory for creating LoginViewModel.
     */
    class Factory(
        private val loginRepository: LoginRepository,
        private val authTokenManager: AuthTokenManager
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(loginRepository, authTokenManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}