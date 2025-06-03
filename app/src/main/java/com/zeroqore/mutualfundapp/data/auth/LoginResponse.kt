// app/src/main/java/com/zeroqore/mutualfundapp/data/auth/LoginResponse.kt
package com.zeroqore.mutualfundapp.data.auth

import com.google.gson.annotations.SerializedName

/**
 * Represents the response body for a successful user login.
 * Contains authentication tokens and user/distributor identifiers.
 */
data class LoginResponse(
    @SerializedName("accessToken")
    val accessToken: String, // The JWT for authentication in subsequent requests
    @SerializedName("refreshToken")
    val refreshToken: String, // Token to obtain new access tokens without re-login
    @SerializedName("expiresIn")
    val expiresIn: Long,      // Access token validity in seconds
    @SerializedName("tokenType")
    val tokenType: String,    // E.g., "Bearer"
    @SerializedName("investorId")
    val investorId: String,   // Unique identifier for the logged-in investor
    @SerializedName("distributorId")
    val distributorId: String,// Unique identifier for the investor's distributor
    @SerializedName("investorName")
    val investorName: String? // Optional: User-friendly name of the investor
)