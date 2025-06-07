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
import com.zeroqore.mutualfundapp.util.Results
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginRepository: LoginRepository,
    private val authTokenManager: AuthTokenManager
) : ViewModel() {

    private val _loginResult = MutableLiveData<Results<LoginResponse>>()
    val loginResult: LiveData<Results<LoginResponse>> = _loginResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun login(username: String, password: String) {
        _isLoading.value = true // Show loading indicator
        _loginResult.value = Results.Loading // Set initial loading state

        viewModelScope.launch {
            val request = LoginRequest(username, password)
            val repoResult: kotlin.Result<LoginResponse> = loginRepository.login(request)

            repoResult
                .onSuccess { loginResponse ->
                    // Convert Long IDs from LoginResponse to String for AuthTokenManager
                    val investorIdString = loginResponse.userId.toString()
                    val distributorIdString = loginResponse.parentId?.toString() // Convert Long? to String?
                    val investorNameString = loginResponse.username // Directly use username which is String?

                    // SAVE AUTH DATA HERE
                    authTokenManager.saveAuthData(
                        accessToken = loginResponse.accessToken,
                        refreshToken = loginResponse.refreshToken,
                        expiresInSeconds = loginResponse.expiresIn, // Updated parameter name
                        tokenType = loginResponse.tokenType,        // Updated parameter name
                        investorId = investorIdString,
                        distributorId = distributorIdString as String?, // KEY CHANGE: Explicitly cast to String?
                        investorName = investorNameString
                    )
                    Log.d("LoginViewModel", "Auth data saved to SharedPreferences. Investor ID: $investorIdString, Distributor ID: $distributorIdString")
                    _loginResult.value = Results.Success(loginResponse)
                }
                .onFailure { exception ->
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