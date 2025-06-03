// app/src/main/java/com/zeroqore/mutualfundapp/data/PortfolioSummary.kt
package com.zeroqore.mutualfundapp.data

// REMOVED: import kotlinx.serialization.Serializable
// ADDED: Optional Gson import if you need @SerializedName annotations, though not strictly needed here
// import com.google.gson.annotations.SerializedName

// REMOVED: @Serializable annotation
data class PortfolioSummary(
    val totalInvested: Double,
    val currentValue: Double,
    val overallGainLoss: Double,
    val overallGainLossPercentage: Double,
    val lastUpdated: String
)