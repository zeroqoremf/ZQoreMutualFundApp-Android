// app/src/main/java/com/zeroqore/mutualfundapp/data/auth/LocalLoginRepository.kt
package com.zeroqore.mutualfundapp.data.auth

import kotlinx.coroutines.delay
import android.util.Log // Import Log for debugging

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
                    // CORRECTED: Use userId, parentId, and username as per updated LoginResponse.kt
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
}