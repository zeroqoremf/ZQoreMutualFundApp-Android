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

    // IMPORTANT: These map backend's generic IAM fields to app-specific names
    @SerializedName("userId") // Backend's 'userId' is a number, map it to Long
    val userId: Long,   // Changed: Use backend's original 'userId' type

    @SerializedName("parentId") // Backend's 'parentId' is a number or null, map it to Long?
    val parentId: Long?,// Changed: Use backend's original 'parentId' type and make it nullable

    @SerializedName("username") // Backend's 'username' will be mapped to 'investorName'
    val username: String?, // Changed: Use backend's original 'username' and make it nullable

    // NEW: Add the roles field to match the backend LoginResponse DTO
    @SerializedName("roles")
    val roles: List<String> // List of roles assigned to the user (e.g., "ROLE_INVESTOR")
)