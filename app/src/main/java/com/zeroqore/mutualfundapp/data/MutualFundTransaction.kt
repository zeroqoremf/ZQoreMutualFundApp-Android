// app/src/main/java/com/zeroqore/mutualfundapp/data/MutualFundTransaction.kt
package com.zeroqore.mutualfundapp.data

data class MutualFundTransaction(
    val transactionId: String,
    val fundName: String,
    val isin: String,
    val transactionDate: String, // Format:YYYY-MM-DD
    val transactionType: String, // e.g., "BUY", "SELL", "SWP", "STP", "DIVIDEND"
    val amount: Double,
    val units: Double,
    val navAtTransaction: Double
)