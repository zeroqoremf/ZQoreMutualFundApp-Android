package com.zeroqore.mutualfundapp.data.auth

import com.google.gson.annotations.SerializedName

/**
 * Represents the request body for initiating a password reset.
 * Sends the user's identifier (e.g., email or username) to the backend.
 */
data class ForgotPasswordRequest(
    @SerializedName("identifier") // This should match the field name your backend expects (e.g., "identifier" or "email")
    val username: String
)