// app/src/main/java/com/zeroqore/mutualfundapp/data/PortfolioSummary.kt
package com.zeroqore.mutualfundapp.data

import kotlinx.serialization.Serializable // Add this import for JSON serialization

@Serializable // Mark data class as serializable
data class PortfolioSummary(
    val totalInvested: Double,
    val currentValue: Double, // Changed from totalCurrentValue to match JSON
    val overallGainLoss: Double,
    val overallGainLossPercentage: Double, // Changed from overallPercentageChange to match JSON
    val lastUpdated: String // New: Added to match JSON
)