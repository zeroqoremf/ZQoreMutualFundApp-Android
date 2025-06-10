package com.zeroqore.mutualfundapp.data.auth

import com.google.gson.annotations.SerializedName

/**
 * Defines a generic error response structure expected from your backend for API errors.
 * This assumes your backend sends error messages in a JSON object with a "message" field,
 * e.g., {"message": "Invalid credentials."}
 */
data class ApiErrorResponse(
    @SerializedName("message") val message: String
)