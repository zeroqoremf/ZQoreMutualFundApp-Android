package com.zeroqore.mutualfundapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MutualFundHolding(
    val fundName: String,
    val isin: String,
    val currentValue: Double,
    val purchasePrice: Double,
    val units: Double,
    val currentNav: Double,
    val purchaseNav: Double,
    val lastUpdated: String,
    val fundType: String,
    val category: String,
    val riskLevel: String,
    val previousDayNav: Double // <-- ADD THIS LINE BACK
) : Parcelable