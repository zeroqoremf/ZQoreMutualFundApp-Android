// app/src/main/java/com/zeroqore/mutualfundapp/data/auth/LoginRepository.kt
package com.zeroqore.mutualfundapp.data.auth

import android.util.Log // Added: For logging errors
import com.google.gson.Gson // Added: For parsing JSON error bodies
import com.google.gson.annotations.SerializedName // Added: For Gson annotation
import com.zeroqore.mutualfundapp.network.AuthService
import retrofit2.HttpException // Added: To catch specific HTTP errors

// 1. Define the Repository Interface (No changes needed here)
interface LoginRepository {
    suspend fun login(request: LoginRequest): Result<LoginResponse>
}

// Optional: Define a generic error response structure expected from your backend.
// This assumes your backend sends error messages in a JSON object with a "message" field,
// e.g., {"message": "Too many failed login attempts. Account has been locked for 15 minutes."}
data class ApiErrorResponse(
    @SerializedName("message") val message: String
)

// 2. Implement the Repository
class NetworkLoginRepository(
    private val authService: AuthService // Inject the AuthService
) : LoginRepository {

    override suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            val response = authService.loginUser(request)
            Result.success(response)
        } catch (e: HttpException) {
            // This block specifically handles HTTP errors (any non-2xx status code)
            val errorBodyString = e.response()?.errorBody()?.string()
            val errorMessage = if (!errorBodyString.isNullOrEmpty()) {
                try {
                    // Attempt to parse the error body as our predefined ApiErrorResponse
                    val errorResponse = Gson().fromJson(errorBodyString, ApiErrorResponse::class.java)
                    // Use the specific message from the backend's error body
                    errorResponse.message
                } catch (jsonException: Exception) {
                    // Fallback if the error body isn't valid JSON or doesn't match our model
                    Log.e("NetworkLoginRepository", "Failed to parse error body JSON for HTTP ${e.code()}: $errorBodyString", jsonException)
                    // Provide a generic message for parsing failures
                    "An unexpected server error occurred (code: ${e.code()}). Please try again."
                }
            } else {
                // Fallback if the error body is completely empty
                when (e.code()) {
                    400 -> "Bad request. Please check your input."
                    401 -> "Invalid username or password. Please try again."
                    403 -> "Access denied. You don't have permission."
                    429 -> "Too many failed login attempts. Your account may be temporarily locked."
                    500 -> "Internal server error. Please try again later."
                    else -> "An unexpected network error occurred (code: ${e.code()})."
                }
            }
            Log.e("NetworkLoginRepository", "HTTP Exception during login: ${e.code()} - $errorMessage", e)
            // Wrap the extracted, user-friendly error message into a new Exception and return a failure Result
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            // This catches any other types of exceptions (e.g., no internet connection, timeouts)
            Log.e("NetworkLoginRepository", "General Exception during login: ${e.message}", e)
            Result.failure(Exception("Could not connect to the server. Please check your internet connection."))
        }
    }
}