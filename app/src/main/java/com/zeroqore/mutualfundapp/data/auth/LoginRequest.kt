// app/src/main/java/com/zeroqore/mutualfundapp/data/auth/LoginRequest.kt
package com.zeroqore.mutualfundapp.data.auth

import com.google.gson.annotations.SerializedName

/**
 * Represents the request body for a user login.
 * Assumes username/password authentication for initial implementation.
 * Can be extended or adapted for other authentication methods (e.g., OTP, social login tokens).
 */
data class LoginRequest(
    @SerializedName("username")
    val username: String, // Typically email or unique identifier
    @SerializedName("password")
    val password: String
)