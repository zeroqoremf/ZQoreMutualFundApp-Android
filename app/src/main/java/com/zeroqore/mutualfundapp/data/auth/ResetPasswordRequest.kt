package com.zeroqore.mutualfundapp.data.auth

import com.google.gson.annotations.SerializedName

/**
 * Represents the request body for confirming a password reset.
 * Includes the received token, the new password, and a confirmation of the new password.
 */
data class ResetPasswordRequest(
    @SerializedName("token")
    val token: String,
    @SerializedName("newPassword")
    val newPassword: String,
    @SerializedName("confirmNewPassword")
    val confirmNewPassword: String
)