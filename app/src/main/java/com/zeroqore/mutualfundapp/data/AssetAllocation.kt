// app/src/main/java/com/zeroqore/mutualfundapp/data/AssetAllocation.kt
package com.zeroqore.mutualfundapp.data

// Data Class: Asset Allocation by Fund Type
data class AssetAllocation(
    val equityValue: Double,
    val equityPercentage: Double,
    val debtValue: Double,
    val debtPercentage: Double,
    val hybridValue: Double,
    val hybridPercentage: Double
    // Add other fund types as needed here
)