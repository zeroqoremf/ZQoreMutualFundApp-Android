// src/main/java/com/zeroqore/mutualfundapp/data/auth/ForgotPasswordInitiateResponse.kt
package com.zeroqore.mutualfundapp.data.auth

import com.google.gson.annotations.SerializedName

/**
 * Represents the successful response body from the backend's forgot-password endpoint.
 * Contains the generated password reset token for internal use.
 */
data class ForgotPasswordInitiateResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("identifier")
    val identifier: String,
    @SerializedName("resetToken")
    val resetToken: String?, // Make it nullable as it might be null for unknown users (security)
    @SerializedName("tokenExpiryMinutes")
    val tokenExpiryMinutes: Int
)