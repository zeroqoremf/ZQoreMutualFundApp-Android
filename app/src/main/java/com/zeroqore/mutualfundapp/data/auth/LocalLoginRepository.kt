package com.zeroqore.mutualfundapp.data.auth

import kotlinx.coroutines.delay
import android.util.Log

class LocalLoginRepository : LoginRepository {

    // Define your dummy credentials here for local testing
    private val DUMMY_USERNAME = "user@example.com"
    private val DUMMY_PASSWORD = "password123"

    /**
     * Simulates a login attempt using hardcoded local data.
     * This bypasses network calls and is useful for development when the backend isn't ready.
     */
    override suspend fun login(request: LoginRequest): Result<LoginResponse> {
        Log.d("LocalLoginRepository", "Attempting local login for: ${request.username}")
        // Simulate network delay to mimic a real API call
        delay(1000) // Simulate a 1-second delay

        return if (request.username == DUMMY_USERNAME && request.password == DUMMY_PASSWORD) {
            Log.d("LocalLoginRepository", "Local login successful.")
            // Return a success Result with dummy data matching LoginResponse constructor
            Result.success(
                LoginResponse(
                    accessToken = "local_dummy_access_token",
                    refreshToken = "local_dummy_refresh_token",
                    expiresIn = 3600L, // 1 hour
                    tokenType = "Bearer",
                    userId = 12345L, // Dummy Long ID for investorId
                    parentId = 67890L, // Dummy Long ID for distributorId, or null if no parent
                    username = "Local Test User" // Dummy username for investorName
                )
            )
        } else {
            Log.d("LocalLoginRepository", "Local login failed: Invalid credentials.")
            // Return a failure Result for incorrect credentials
            Result.failure(Exception("Invalid username or password."))
        }
    }

    /**
     * Simulates a request for password reset.
     * Always succeeds locally for `DUMMY_USERNAME` after a delay.
     */
    override suspend fun requestPasswordReset(request: ForgotPasswordRequest): Result<ForgotPasswordInitiateResponse> { // <--- CHANGED RETURN TYPE
        Log.d("LocalLoginRepository", "Simulating local password reset request for: ${request.username}")
        delay(800) // Simulate a short delay

        // You can add logic here if you want to simulate failure for non-dummy users,
        // or always succeed for testing the UI flow.
        return if (request.username == DUMMY_USERNAME) {
            Log.d("LocalLoginRepository", "Local password reset request successful for ${request.username}.")
            // Return ForgotPasswordInitiateResponse with dummy token
            Result.success(
                ForgotPasswordInitiateResponse(
                    message = "Dummy reset link sent locally. Token: dummy-local-token",
                    identifier = request.username,
                    resetToken = "dummy-local-reset-token-${System.currentTimeMillis()}", // Unique dummy token
                    tokenExpiryMinutes = 30
                )
            )
        } else {
            Log.e("LocalLoginRepository", "Local password reset request failed for ${request.username}: User not found.")
            Result.failure(Exception("User not found or email could not be sent.")) // Simulate a common error
        }
    }

    /**
     * Simulates a confirmation of password reset.
     * Always succeeds locally after a delay, regardless of token or new password.
     */
    override suspend fun confirmPasswordReset(request: ResetPasswordRequest): Result<Unit> {
        Log.d("LocalLoginRepository", "Simulating local password reset confirmation.")
        delay(1200) // Simulate a short delay

        // For local testing, you might just always succeed here to test the UI flow.
        // Or add logic to simulate invalid token/password mismatch errors.
        Log.d("LocalLoginRepository", "Local password reset confirmation successful.")
        return Result.success(Unit) // Return Unit on success
    }
}