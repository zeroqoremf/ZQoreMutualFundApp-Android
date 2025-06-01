// app/src/main/java/com/zeroqore/mutualfundapp/data/AppContainer.kt
package com.zeroqore.mutualfundapp.data

import java.text.NumberFormat
import java.util.Locale

// Existing Data Class: Mutual Fund Holding (should be in its own file)
// No change here, ensuring it's not re-declared.

// Existing Data Class: Portfolio Summary (retained)
data class PortfolioSummary(
    val totalInvested: Double,
    val totalCurrentValue: Double,
    val overallGainLoss: Double,
    val overallPercentageChange: Double
)

// Existing Data Class: Asset Allocation by Fund Type (retained)
data class AssetAllocation(
    val equityValue: Double,
    val equityPercentage: Double,
    val debtValue: Double,
    val debtPercentage: Double,
    val hybridValue: Double,
    val hybridPercentage: Double
    // Add other fund types as needed
)

// Existing Data Class: Mutual Fund Transaction (retained)
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

// NEW Data Class: MenuItem
data class MenuItem(
    val id: String,
    val title: String,
    val description: String? = null // Optional description for the menu item
    // Add an icon resource ID if we decide to use icons later
)

// Interface for the Mutual Fund Repository
interface MutualFundRepository {
    fun getFundHoldings(): List<MutualFundHolding>
    fun getPortfolioSummary(): PortfolioSummary
    fun getAssetAllocation(): AssetAllocation
    fun getTransactions(): List<MutualFundTransaction>
    fun getMenuItems(): List<MenuItem> // NEW method
}

// MOCK Implementation of the Repository
class MockMutualFundRepository : MutualFundRepository {

    // Re-using the same dummy holdings to calculate portfolio data
    private val dummyHoldings = listOf(
        MutualFundHolding(
            fundName = "Axis Bluechip Fund - Growth",
            isin = "INF846K01802",
            currentValue = 15000.0,
            purchasePrice = 12000.0,
            units = 100.0,
            currentNav = 150.0,
            purchaseNav = 120.0,
            lastUpdated = "2024-05-29",
            fundType = "Equity",
            category = "Large Cap",
            riskLevel = "High",
            previousDayNav = 148.0
        ),
        MutualFundHolding(
            fundName = "SBI Small Cap Fund - Growth",
            isin = "INF200K01673",
            currentValue = 25000.0,
            purchasePrice = 28000.0,
            units = 50.0,
            currentNav = 500.0,
            purchaseNav = 560.0,
            lastUpdated = "2024-05-29",
            fundType = "Equity",
            category = "Small Cap",
            riskLevel = "Very High",
            previousDayNav = 510.0
        ),
        MutualFundHolding(
            fundName = "ICICI Prudential Balanced Advantage Fund - Growth",
            isin = "INF761K01912",
            currentValue = 8000.0,
            purchasePrice = 7500.0,
            units = 80.0,
            currentNav = 100.0,
            purchaseNav = 93.75,
            lastUpdated = "2024-05-29",
            fundType = "Hybrid",
            category = "Dynamic Asset Allocation",
            riskLevel = "Moderate",
            previousDayNav = 99.0
        )
    )

    // Dummy Transactions data (retained)
    private val dummyTransactions = listOf(
        MutualFundTransaction(
            transactionId = "TXN001",
            fundName = "Axis Bluechip Fund - Growth",
            isin = "INF846K01802",
            transactionDate = "2024-01-15",
            transactionType = "BUY",
            amount = 12000.0,
            units = 100.0,
            navAtTransaction = 120.0
        ),
        MutualFundTransaction(
            transactionId = "TXN002",
            fundName = "SBI Small Cap Fund - Growth",
            isin = "INF200K01673",
            transactionDate = "2023-11-20",
            transactionType = "BUY",
            amount = 28000.0,
            units = 50.0,
            navAtTransaction = 560.0
        ),
        MutualFundTransaction(
            transactionId = "TXN003",
            fundName = "ICICI Prudential Balanced Advantage Fund - Growth",
            isin = "INF761K01912",
            transactionDate = "2024-03-10",
            transactionType = "BUY",
            amount = 7500.0,
            units = 80.0,
            navAtTransaction = 93.75
        ),
        MutualFundTransaction(
            transactionId = "TXN004",
            fundName = "Axis Bluechip Fund - Growth",
            isin = "INF846K01802",
            transactionDate = "2024-05-20",
            transactionType = "DIVIDEND",
            amount = 500.0,
            units = 0.0, // No units change for dividend payout
            navAtTransaction = 145.0 // NAV on dividend date
        ),
        MutualFundTransaction(
            transactionId = "TXN005",
            fundName = "SBI Small Cap Fund - Growth",
            isin = "INF200K01673",
            transactionDate = "2024-06-01", // Today's date (mock)
            transactionType = "SELL", // Example sell
            amount = 5000.0, // Amount received
            units = 10.0, // Units sold
            navAtTransaction = 500.0 // NAV at transaction
        )
    )

    // NEW: Dummy Menu Items data
    private val dummyMenuItems = listOf(
        MenuItem(id = "profile", title = "My Profile"),
        MenuItem(id = "settings", title = "Settings"),
        MenuItem(id = "statements", title = "Statements"),
        MenuItem(id = "contact", title = "Contact Us"),
        MenuItem(id = "about", title = "About App"),
        MenuItem(id = "logout", title = "Logout")
    )

    override fun getFundHoldings(): List<MutualFundHolding> {
        return dummyHoldings
    }

    override fun getPortfolioSummary(): PortfolioSummary {
        var totalInvested = 0.0
        var totalCurrentValue = 0.0

        for (holding in dummyHoldings) {
            totalInvested += (holding.purchasePrice * holding.units)
            totalCurrentValue += holding.currentValue
        }

        val overallGainLoss = totalCurrentValue - totalInvested
        val overallPercentageChange = if (totalInvested != 0.0) {
            (overallGainLoss / totalInvested) * 100
        } else {
            0.0
        }

        return PortfolioSummary(
            totalInvested = totalInvested,
            totalCurrentValue = totalCurrentValue,
            overallGainLoss = overallGainLoss,
            overallPercentageChange = overallPercentageChange
        )
    }

    override fun getAssetAllocation(): AssetAllocation {
        var equityValue = 0.0
        var debtValue = 0.0
        var hybridValue = 0.0
        var totalCurrentValue = 0.0

        for (holding in dummyHoldings) {
            when (holding.fundType) {
                "Equity" -> equityValue += holding.currentValue
                "Debt" -> debtValue += holding.currentValue
                "Hybrid" -> hybridValue += holding.currentValue
                // Add other types as needed
            }
            totalCurrentValue += holding.currentValue
        }

        val equityPercentage = if (totalCurrentValue != 0.0) (equityValue / totalCurrentValue) * 100 else 0.0
        val debtPercentage = if (totalCurrentValue != 0.0) (debtValue / totalCurrentValue) * 100 else 0.0
        val hybridPercentage = if (totalCurrentValue != 0.0) (hybridValue / totalCurrentValue) * 100 else 0.0

        return AssetAllocation(
            equityValue = equityValue,
            equityPercentage = equityPercentage,
            debtValue = debtValue,
            debtPercentage = debtPercentage,
            hybridValue = hybridValue,
            hybridPercentage = hybridPercentage
        )
    }

    override fun getTransactions(): List<MutualFundTransaction> {
        return dummyTransactions.sortedByDescending { it.transactionDate } // Sort by date for display
    }

    // NEW: Implement getMenuItems()
    override fun getMenuItems(): List<MenuItem> {
        return dummyMenuItems
    }
}

// Simple AppContainer to provide dependencies
class AppContainer {
    val mutualFundRepository: MutualFundRepository = MockMutualFundRepository()
}