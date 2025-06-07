package com.zeroqore.mutualfundapp.data.auth

import com.zeroqore.mutualfundapp.network.AuthService
import retrofit2.HttpException
import java.io.IOException
import android.util.Log // Import Log for debugging

/**
 * Repository implementation that makes actual network calls for login.
 */
class RemoteLoginRepository(private val authService: AuthService) : LoginRepository {

    override suspend fun login(request: LoginRequest): Result<LoginResponse> {
        Log.d("RemoteLoginRepository", "Attempting remote login for: ${request.username}")
        return try {
            // Make the actual network call using AuthService
            val response = authService.loginUser(request)
            Log.d("RemoteLoginRepository", "Remote login successful for: ${request.username}")
            Result.success(response)
        } catch (e: HttpException) {
            // Handle HTTP errors (e.g., 400 Bad Request, 401 Unauthorized)
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("RemoteLoginRepository", "HTTP error during login: ${e.code()} - $errorBody", e)
            Result.failure(Exception("Login failed: ${errorBody ?: e.message()}"))
        } catch (e: IOException) {
            // Handle network connectivity errors (e.g., no internet, host unreachable)
            Log.e("RemoteLoginRepository", "Network error during login: ${e.message}", e)
            Result.failure(Exception("Network error: Please check your internet connection."))
        } catch (e: Exception) {
            // Handle any other unexpected errors
            Log.e("RemoteLoginRepository", "Unexpected error during login: ${e.message}", e)
            Result.failure(Exception("An unexpected error occurred."))
        }
    }
}