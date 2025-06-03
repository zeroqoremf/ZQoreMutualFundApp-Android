package com.zeroqore.mutualfundapp.data.auth

import com.zeroqore.mutualfundapp.network.AuthService

// 1. Define the Repository Interface
interface LoginRepository {
    suspend fun login(request: LoginRequest): Result<LoginResponse>
}

// 2. Implement the Repository
class NetworkLoginRepository(
    private val authService: AuthService // Inject the AuthService
) : LoginRepository {

    override suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            val response = authService.loginUser(request)
            Result.success(response)
        } catch (e: Exception) {
            // Log the exception for debugging
            e.printStackTrace()
            Result.failure(e) // Wrap the exception in a Result.failure
        }
    }
}