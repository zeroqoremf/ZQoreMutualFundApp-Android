// app/src/main/java/com/zeroqore/mutualfundapp/data/MutualFundAppRepository.kt
package com.zeroqore.mutualfundapp.data

import com.zeroqore.mutualfundapp.network.MutualFundApiService

// Renamed Interface for the Mutual Fund Repository
interface MutualFundAppRepository {
    suspend fun getFundHoldings(): List<MutualFundHolding>
    suspend fun getHoldings(): List<MutualFundHolding>
    suspend fun getMenuItems(): List<MenuItem>
    suspend fun getPortfolioSummary(): PortfolioSummary // Interface already correct
    suspend fun getAssetAllocation(): AssetAllocation
    suspend fun getTransactions(): List<MutualFundTransaction>
}

// Renamed Concrete implementation of the Repository that uses the network service
class NetworkMutualFundAppRepository(private val apiService: MutualFundApiService) : MutualFundAppRepository {

    override suspend fun getFundHoldings(): List<MutualFundHolding> {
        return apiService.getFundHoldings()
    }

    override suspend fun getHoldings(): List<MutualFundHolding> {
        return apiService.getHoldings()
    }

    override suspend fun getMenuItems(): List<MenuItem> {
        // For now, returning dummy data
        return listOf(
            MenuItem(id = "profile", title = "My Profile"),
            MenuItem(id = "settings", title = "Settings"),
            MenuItem(id = "statements", title = "Statements"),
            MenuItem(id = "contact", title = "Contact Us"),
            MenuItem(id = "about", title = "About App"),
            MenuItem(id = "logout", title = "Logout")
        )
    }

    // *** IMPORTANT CHANGE HERE FOR PORTFOLIO SUMMARY ***
    override suspend fun getPortfolioSummary(): PortfolioSummary {
        // Now calling the API service to fetch the summary from portfolio_summary.json
        return apiService.getPortfolioSummary()
    }

    override suspend fun getAssetAllocation(): AssetAllocation {
        // TODO: Update this to call apiService.getAssetAllocation() once you have a dedicated asset_allocation.json
        val holdings = getFundHoldings() // Currently still calculates based on holdings
        var equityValue = 0.0
        var debtValue = 0.0
        var hybridValue = 0.0
        var totalCurrentValue = 0.0

        for (holding in holdings) {
            when (holding.fundType) {
                "Equity" -> equityValue += holding.currentValue
                "Debt" -> debtValue += holding.currentValue
                "Hybrid" -> hybridValue += holding.currentValue
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

    override suspend fun getTransactions(): List<MutualFundTransaction> {
        // TODO: This should now call apiService.getTransactions() to fetch from transactions.json
        // For now, returning dummy data.
        return listOf(
            MutualFundTransaction(
                transactionId = "TRN001",
                fundName = "Axis Bluechip Fund - Growth",
                isin = "INF846K01802",
                transactionDate = "2024-05-15",
                transactionType = "BUY",
                amount = 12000.0,
                units = 100.0,
                navAtTransaction = 120.0
            ),
            MutualFundTransaction(
                transactionId = "TRN002",
                fundName = "SBI Small Cap Fund - Growth",
                isin = "INF200K01673",
                transactionDate = "2024-04-20",
                transactionType = "BUY",
                amount = 28000.0,
                units = 50.0,
                navAtTransaction = 560.0
            ),
            MutualFundTransaction(
                transactionId = "TRN003",
                fundName = "ICICI Prudential Balanced Advantage Fund - Growth",
                isin = "INF761K01912",
                transactionDate = "2024-03-10",
                transactionType = "BUY",
                amount = 7500.0,
                units = 80.0,
                navAtTransaction = 93.75
            )
        )
    }
}

// Renamed Optional: Mock implementation of the Repository
class MockMutualFundAppRepository : MutualFundAppRepository {
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

    // *** NEW: Add dummy portfolio summary data for mock implementation ***
    private val dummyPortfolioSummary = PortfolioSummary(
        totalInvested = 150000.0,
        currentValue = 165000.0, // Corrected key
        overallGainLoss = 15000.0,
        overallGainLossPercentage = 10.0, // Corrected key
        lastUpdated = "2024-06-01T14:30:00Z"
    )

    private val dummyTransactions = listOf(
        MutualFundTransaction(
            transactionId = "MOCKTRN001",
            fundName = "Mock Fund X",
            isin = "MOCKX001",
            transactionDate = "2024-05-25",
            transactionType = "BUY",
            amount = 5000.0,
            units = 20.0,
            navAtTransaction = 250.0
        ),
        MutualFundTransaction(
            transactionId = "MOCKTRN002",
            fundName = "Mock Fund Y",
            isin = "MOCKY001",
            transactionDate = "2024-05-10",
            transactionType = "SELL",
            amount = 1000.0,
            units = 5.0,
            navAtTransaction = 200.0
        )
    )

    override suspend fun getFundHoldings(): List<MutualFundHolding> {
        return dummyHoldings
    }

    override suspend fun getHoldings(): List<MutualFundHolding> {
        return dummyHoldings
    }

    override suspend fun getMenuItems(): List<MenuItem> {
        return listOf(
            MenuItem(id = "mock_profile", title = "Mock Profile"),
            MenuItem(id = "mock_settings", title = "Mock Settings")
        )
    }

    // *** IMPORTANT CHANGE HERE FOR MOCK PORTFOLIO SUMMARY ***
    override suspend fun getPortfolioSummary(): PortfolioSummary {
        return dummyPortfolioSummary
    }

    override suspend fun getAssetAllocation(): AssetAllocation {
        var equityValue = 0.0
        var debtValue = 0.0
        var hybridValue = 0.0
        var totalCurrentValue = 0.0

        for (holding in dummyHoldings) {
            when (holding.fundType) {
                "Equity" -> equityValue += holding.currentValue
                "Debt" -> debtValue += holding.currentValue
                "Hybrid" -> hybridValue += holding.currentValue
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

    override suspend fun getTransactions(): List<MutualFundTransaction> {
        return dummyTransactions
    }
}