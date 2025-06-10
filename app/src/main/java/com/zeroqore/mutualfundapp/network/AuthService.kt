// src/main/java/com/zeroqore/mutualfundapp/network/AuthService.kt
package com.zeroqore.mutualfundapp.network

import com.zeroqore.mutualfundapp.data.auth.ForgotPasswordRequest
import com.zeroqore.mutualfundapp.data.auth.LoginRequest
import com.zeroqore.mutualfundapp.data.auth.LoginResponse
import com.zeroqore.mutualfundapp.data.auth.ResetPasswordRequest
import com.zeroqore.mutualfundapp.data.auth.ForgotPasswordInitiateResponse // NEW IMPORT HERE

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit interface for authentication related API calls.
 */
interface AuthService {

    @POST("api/v1/auth/login")
    suspend fun loginUser(@Body request: LoginRequest): LoginResponse

    // --- NEW: Password Reset Endpoints ---

    @POST("api/v1/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): ForgotPasswordInitiateResponse // <-- CHANGE RETURN TYPE HERE

    @POST("api/v1/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest)
}