// app/src/main/java/com/zeroqore/mutualfundapp/data/PortfolioDisplayItem.kt
package com.zeroqore.mutualfundapp.data

sealed class PortfolioDisplayItem {
    // Represents a header for an asset type (e.g., Equity, Debt)
    data class AssetTypeHeader(
        val id: String, // Unique ID for DiffUtil (e.g., "Equity_Header")
        val fundType: String,
        val totalCurrentValue: Double,
        val totalInvestedValue: Double, // Added for overall gain/loss calc per type
        val gainLossAbsolute: Double, // Calculated gain/loss for this asset type
        val gainLossPercentage: Double, // Calculated percentage for this asset type
        var isExpanded: Boolean = false // State for expand/collapse
    ) : PortfolioDisplayItem() {
        val uniqueId = id // Used for DiffUtil's getItemId
    }

    // Represents an individual mutual fund holding under an asset type
    data class FundHoldingItem(
        val holding: MutualFundHolding,
        val absoluteReturn: Double, // current - purchase for this specific holding
        val percentageReturn: Double // current - purchase % for this specific holding
    ) : PortfolioDisplayItem() {
        val uniqueId = holding.isin // Use ISIN as unique ID for DiffUtil
    }
}