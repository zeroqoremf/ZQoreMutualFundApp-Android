package com.zeroqore.mutualfundapp.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeroqore.mutualfundapp.data.auth.ForgotPasswordRequest
import com.zeroqore.mutualfundapp.data.auth.ForgotPasswordInitiateResponse
import com.zeroqore.mutualfundapp.data.auth.LoginRepository
import com.zeroqore.mutualfundapp.data.auth.ResetPasswordRequest
import com.zeroqore.mutualfundapp.util.Event
import com.zeroqore.mutualfundapp.util.Results
import kotlinx.coroutines.launch
import android.util.Log

// NEW IMPORTS FOR LOGIN FUNCTIONALITY
import com.zeroqore.mutualfundapp.data.auth.LoginRequest
import com.zeroqore.mutualfundapp.data.auth.LoginResponse

class AuthViewModel(private val repository: LoginRepository) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData to expose login results, including tokens and roles
    // The Event wrapper ensures the login result is consumed only once by the UI
    private val _loginResult = MutableLiveData<Event<Results<LoginResponse>>>()
    val loginResult: LiveData<Event<Results<LoginResponse>>> = _loginResult

    // Existing LiveData for password reset flows
    private val _forgotPasswordResult = MutableLiveData<Event<Results<ForgotPasswordInitiateResponse>>>()
    val forgotPasswordResult: LiveData<Event<Results<ForgotPasswordInitiateResponse>>> = _forgotPasswordResult

    private val _resetPasswordResult = MutableLiveData<Event<Results<Unit>>>()
    val resetPasswordResult: LiveData<Event<Results<Unit>>> = _resetPasswordResult

    /**
     * Handles user login request.
     * Calls the repository to authenticate the user and updates loginResult LiveData.
     * @param request The LoginRequest DTO containing identifier and password.
     */
    fun login(request: LoginRequest) {
        _isLoading.value = true // Show loading indicator
        _loginResult.value = Event(Results.Loading) // Indicate login is in progress

        viewModelScope.launch {
            val repoResult: kotlin.Result<LoginResponse> = repository.login(request)

            repoResult
                .onSuccess { response ->
                    // Log the roles for verification
                    Log.d("AuthViewModel", "Login successful for user: ${response.username}, Roles received: ${response.roles}")

                    // Post the successful LoginResponse to be observed by the UI
                    _loginResult.postValue(Event(Results.Success(response)))

                    // OPTIONAL: In a real app, you would likely save tokens and roles here
                    // e.g., in SharedPreferences or DataStore for session management.
                    // This is outside the scope of *just* exposing roles for UI visibility,
                    // but important for persistent login.
                    // saveAuthTokens(response.accessToken, response.refreshToken)
                    // saveUserRoles(response.roles)
                }
                .onFailure { exception ->
                    // Handle login failure
                    val errorMessage = exception.message ?: "Unknown login error."
                    _loginResult.postValue(Event(Results.Error(exception, errorMessage)))
                    Log.e("AuthViewModel", "Login failed: $errorMessage", exception)
                }
            _isLoading.postValue(false) // Hide loading indicator
        }
    }

    /**
     * Initiates the forgot password flow.
     * Requests a reset link to be sent to the given email/username.
     */
    fun requestPasswordReset(username: String) {
        _isLoading.value = true
        _forgotPasswordResult.value = Event(Results.Loading)

        viewModelScope.launch {
            val request = ForgotPasswordRequest(username = username)
            val repoResult: kotlin.Result<ForgotPasswordInitiateResponse> = repository.requestPasswordReset(request)

            repoResult
                .onSuccess { response ->
                    _forgotPasswordResult.postValue(Event(Results.Success(response)))
                    Log.d("AuthViewModel", "Password reset request successful for ${response.identifier}. Token: ${response.resetToken}")
                }
                .onFailure { exception ->
                    val errorMessage = exception.message ?: "Unknown error requesting password reset."
                    _forgotPasswordResult.postValue(Event(Results.Error(exception, errorMessage)))
                    Log.e("AuthViewModel", "Password reset request failed: $errorMessage", exception)
                }
            _isLoading.postValue(false)
        }
    }

    /**
     * Confirms the password reset with the token and new password.
     *
     * IMPORTANT CHANGE: This function now accepts a ResetPasswordRequest object directly.
     */
    fun confirmPasswordReset(request: ResetPasswordRequest) {
        if (request.newPassword != request.confirmNewPassword) {
            _resetPasswordResult.postValue(Event(Results.Error(IllegalArgumentException("Passwords do not match."), "Passwords do not match.")))
            return
        }

        _isLoading.value = true
        _resetPasswordResult.value = Event(Results.Loading)

        viewModelScope.launch {
            val repoResult: kotlin.Result<Unit> = repository.confirmPasswordReset(request)

            repoResult
                .onSuccess {
                    _resetPasswordResult.postValue(Event(Results.Success(Unit)))
                    Log.d("AuthViewModel", "Password reset confirmation successful.")
                }
                .onFailure { exception ->
                    val errorMessage = exception.message ?: "Unknown error resetting password."
                    _resetPasswordResult.postValue(Event(Results.Error(exception, errorMessage)))
                    Log.e("AuthViewModel", "Password reset confirmation failed: $errorMessage", exception)
                }
            _isLoading.postValue(false)
        }
    }
}