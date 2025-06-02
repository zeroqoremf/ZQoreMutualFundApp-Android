// app/src/main/java/com/zeroqore/mutualfundapp/data/Fund.kt
package com.zeroqore.mutualfundapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Fund(
    val fundName: String,
    val isin: String, // International Securities Identification Number - Unique ID for the fund
    val currentNav: Double,
    val previousDayNav: Double,
    val fundType: String, // e.g., "Equity", "Debt", "Hybrid", "ELSS"
    val category: String, // e.g., "Large Cap", "Small Cap", "Liquid", "Balanced Advantage"
    val riskLevel: String, // e.g., "Low", "Moderate", "High", "Very High"
    val aum: Double, // Assets Under Management in crores/millions (e.g., 50000.0 for 50,000 Cr)
    val minInvestment: Double, // Minimum initial investment amount
    val expenseRatio: Double, // Annual expense ratio as a percentage (e.g., 0.5 for 0.5%)
    val oneYearReturn: Double, // 1-year return percentage (e.g., 15.2 for 15.2%)
    val threeYearReturn: Double, // 3-year return percentage
    val fiveYearReturn: Double, // 5-year return percentage
    val fundHouse: String // Name of the Asset Management Company (AMC)
) : Parcelable