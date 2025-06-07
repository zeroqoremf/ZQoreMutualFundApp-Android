package com.zeroqore.mutualfundapp.network

import com.zeroqore.mutualfundapp.data.auth.LoginRequest
import com.zeroqore.mutualfundapp.data.auth.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit interface for authentication related API calls.
 */
interface AuthService {

    @POST("api/v1/auth/login") // <--- CHANGED THIS LINE
    suspend fun loginUser(@Body request: LoginRequest): LoginResponse
}