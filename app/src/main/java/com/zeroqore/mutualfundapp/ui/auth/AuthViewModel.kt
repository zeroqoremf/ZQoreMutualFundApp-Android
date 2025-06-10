package com.zeroqore.mutualfundapp.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeroqore.mutualfundapp.data.auth.ForgotPasswordRequest
import com.zeroqore.mutualfundapp.data.auth.ForgotPasswordInitiateResponse // NEW IMPORT
import com.zeroqore.mutualfundapp.data.auth.LoginRepository
import com.zeroqore.mutualfundapp.data.auth.ResetPasswordRequest
import com.zeroqore.mutualfundapp.util.Event
import com.zeroqore.mutualfundapp.util.Results
import kotlinx.coroutines.launch
import android.util.Log

class AuthViewModel(private val repository: LoginRepository) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // CHANGE: The LiveData now holds ForgotPasswordInitiateResponse on success
    private val _forgotPasswordResult = MutableLiveData<Event<Results<ForgotPasswordInitiateResponse>>>()
    val forgotPasswordResult: LiveData<Event<Results<ForgotPasswordInitiateResponse>>> = _forgotPasswordResult

    private val _resetPasswordResult = MutableLiveData<Event<Results<Unit>>>()
    val resetPasswordResult: LiveData<Event<Results<Unit>>> = _resetPasswordResult

    /**
     * Initiates the forgot password flow.
     * Requests a reset link to be sent to the given email/username.
     */
    fun requestPasswordReset(username: String) {
        _isLoading.value = true
        // When setting Results.Loading, we don't have the data yet, so cast to correct type if needed.
        // For now, we'll just set it, and the type inference will handle the later success/error.
        _forgotPasswordResult.value = Event(Results.Loading)

        viewModelScope.launch {
            val request = ForgotPasswordRequest(username = username)
            // CHANGE: repoResult now expects ForgotPasswordInitiateResponse
            val repoResult: kotlin.Result<ForgotPasswordInitiateResponse> = repository.requestPasswordReset(request)

            repoResult
                .onSuccess { response -> // CHANGE: 'it' is now 'response' which is ForgotPasswordInitiateResponse
                    // Pass the actual response object to Results.Success
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