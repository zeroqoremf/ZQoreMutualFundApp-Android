package com.zeroqore.mutualfundapp.data.auth

import android.util.Log
import com.google.gson.Gson
import com.zeroqore.mutualfundapp.network.AuthService
import retrofit2.HttpException
import java.io.IOException
import com.zeroqore.mutualfundapp.data.auth.ApiErrorResponse

/**
 * Repository implementation that makes actual network calls for login AND password reset flows.
 * This class implements the updated LoginRepository interface.
 */
class RemoteLoginRepository(
    private val authService: AuthService // Inject the AuthService
) : LoginRepository {

    override suspend fun login(request: LoginRequest): Result<LoginResponse> {
        Log.d("RemoteLoginRepository", "Attempting remote login for: ${request.username}")
        return try {
            val response = authService.loginUser(request)
            Log.d("RemoteLoginRepository", "Remote login successful for: ${request.username}")
            Result.success(response)
        } catch (e: HttpException) {
            handleHttpException(e, "login")
        } catch (e: IOException) {
            handleGeneralException(e, "login")
        } catch (e: Exception) {
            handleGeneralException(e, "login")
        }
    }

    // --- UPDATED: Implementation for Password Reset Functions ---

    override suspend fun requestPasswordReset(request: ForgotPasswordRequest): Result<ForgotPasswordInitiateResponse> { // <-- CHANGE RETURN TYPE HERE
        Log.d("RemoteLoginRepository", "Attempting remote password reset request for: ${request.username}")
        return try {
            val response = authService.forgotPassword(request) // <-- CAPTURE THE RESPONSE FROM AUTHSERVICE
            Log.d("RemoteLoginRepository", "Remote password reset request successful for: ${request.username}")
            Result.success(response) // <-- RETURN THE CAPTURED RESPONSE
        } catch (e: HttpException) {
            handleHttpException(e, "request password reset")
        } catch (e: IOException) {
            handleGeneralException(e, "request password reset")
        } catch (e: Exception) {
            handleGeneralException(e, "request password reset")
        }
    }

    override suspend fun confirmPasswordReset(request: ResetPasswordRequest): Result<Unit> {
        Log.d("RemoteLoginRepository", "Attempting remote password reset confirmation.")
        return try {
            authService.resetPassword(request)
            Log.d("RemoteLoginRepository", "Remote password reset confirmation successful.")
            Result.success(Unit)
        } catch (e: HttpException) {
            handleHttpException(e, "confirm password reset")
        } catch (e: IOException) {
            handleGeneralException(e, "confirm password reset")
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
                Log.e("RemoteLoginRepository", "Failed to parse error body JSON for HTTP ${e.code()} during $operation: $errorBodyString", jsonException)
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
        Log.e("RemoteLoginRepository", "HTTP Exception during $operation: ${e.code()} - $errorMessage", e)
        return Result.failure(Exception(errorMessage))
    }

    private fun <T> handleGeneralException(e: Exception, operation: String): Result<T> {
        Log.e("RemoteLoginRepository", "General Exception during $operation: ${e.message}", e)
        val errorMessage = when (e) {
            is IOException -> "Could not connect to the server. Please check your internet connection."
            else -> "An unexpected error occurred: ${e.message ?: "Unknown error"}"
        }
        return Result.failure(Exception(errorMessage))
    }
}