// src/main/java/com/zeroqore/mutualfundapp/data/auth/LoginRepository.kt
package com.zeroqore.mutualfundapp.data.auth

import android.util.Log
import com.google.gson.Gson
import com.zeroqore.mutualfundapp.network.AuthService
import retrofit2.HttpException
import com.zeroqore.mutualfundapp.data.auth.ApiErrorResponse

// 1. Define the Repository Interface (Already Updated)
interface LoginRepository {
    suspend fun login(request: LoginRequest): Result<LoginResponse>

    /**
     * Requests a password reset link to be sent to the provided identifier (email/username).
     * @param request The ForgotPasswordRequest containing the user's identifier.
     * @return A [Result] indicating success or failure. On success, returns [ForgotPasswordInitiateResponse].
     */
    suspend fun requestPasswordReset(request: ForgotPasswordRequest): Result<ForgotPasswordInitiateResponse> // <-- Interface remains the same

    /**
     * Confirms the password reset by providing the token and new password.
     * @param request The ResetPasswordRequest containing the token and new password details.
     * @return A [Result] indicating success or failure. [Unit] on success signifies no specific data returned.
     */
    suspend fun confirmPasswordReset(request: ResetPasswordRequest): Result<Unit>
}

// 2. Implement the Repository (UPDATED)
class NetworkLoginRepository(
    private val authService: AuthService // Inject the AuthService
) : LoginRepository {

    override suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            val response = authService.loginUser(request)
            Result.success(response)
        } catch (e: HttpException) {
            handleHttpException(e, "login")
        } catch (e: Exception) {
            handleGeneralException(e, "login")
        }
    }

    // --- UPDATED: Implementation for requestPasswordReset Function ---

    override suspend fun requestPasswordReset(request: ForgotPasswordRequest): Result<ForgotPasswordInitiateResponse> { // <-- CHANGE RETURN TYPE HERE
        return try {
            val response = authService.forgotPassword(request) // <-- CAPTURE THE RESPONSE
            Result.success(response) // <-- RETURN THE CAPTURED RESPONSE
        } catch (e: HttpException) {
            handleHttpException(e, "request password reset")
        } catch (e: Exception) {
            handleGeneralException(e, "request password reset")
        }
    }

    override suspend fun confirmPasswordReset(request: ResetPasswordRequest): Result<Unit> {
        return try {
            authService.resetPassword(request)
            Result.success(Unit)
        } catch (e: HttpException) {
            handleHttpException(e, "confirm password reset")
        } catch (e: Exception) {
            handleGeneralException(e, "confirm password reset")
        }
    }

    // --- Private Helper Functions for Error Handling (Remains the same) ---

    private fun <T> handleHttpException(e: HttpException, operation: String): Result<T> {
        val errorBodyString = e.response()?.errorBody()?.string()
        val errorMessage = if (!errorBodyString.isNullOrEmpty()) {
            try {
                val errorResponse = Gson().fromJson(errorBodyString, ApiErrorResponse::class.java)
                errorResponse.message
            } catch (jsonException: Exception) {
                Log.e("NetworkLoginRepository", "Failed to parse error body JSON for HTTP ${e.code()} during $operation: $errorBodyString", jsonException)
                "An unexpected server error occurred (code: ${e.code()}). Please try again."
            }
        } else {
            when (e.code()) {
                400 -> "Bad request. Please check your input."
                401 -> "Unauthorized. Please check credentials or token."
                403 -> "Access denied. You don't have permission."
                404 -> "Resource not found. Please check the URL."
                429 -> "Too many requests. Please try again later."
                500 -> "Internal server error. Please try again later."
                else -> "An unexpected network error occurred (code: ${e.code()})."
            }
        }
        Log.e("NetworkLoginRepository", "HTTP Exception during $operation: ${e.code()} - $errorMessage", e)
        return Result.failure(Exception(errorMessage))
    }

    private fun <T> handleGeneralException(e: Exception, operation: String): Result<T> {
        Log.e("NetworkLoginRepository", "General Exception during $operation: ${e.message}", e)
        val errorMessage = when (e) {
            is java.io.IOException -> "Could not connect to the server. Please check your internet connection."
            else -> "An unexpected error occurred: ${e.message ?: "Unknown error"}"
        }
        return Result.failure(Exception(errorMessage))
    }
}