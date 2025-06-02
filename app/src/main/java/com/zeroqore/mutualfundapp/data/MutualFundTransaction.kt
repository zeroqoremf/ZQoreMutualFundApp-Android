package com.zeroqore.mutualfundapp.data

import com.google.gson.annotations.SerializedName // Import this line

data class MutualFundTransaction(
    val transactionId: String,
    val fundName: String,
    val isin: String,
    val transactionDate: String, // Format:YYYY-MM-DD
    @SerializedName("type") // Add this annotation
    val transactionType: String?, // e.g., "BUY", "SELL", "SWP", "STP", "DIVIDEND" - MADE NULLABLE
    val amount: Double,
    val units: Double,
    val navAtTransaction: Double
)