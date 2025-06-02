// app/src/main/java/com/zeroqore/mutualfundapp/data/MutualFundAppRepository.kt
package com.zeroqore.mutualfundapp.data

import com.zeroqore.mutualfundapp.network.MutualFundApiService

// Renamed Interface for the Mutual Fund Repository
interface MutualFundAppRepository {
    // Keep for now, but consider removing later as it's deprecated in API service
    suspend fun getFundHoldings(): List<MutualFundHolding>
    suspend fun getHoldings(): List<MutualFundHolding>
    suspend fun getMenuItems(): List<MenuItem>
    suspend fun getPortfolioSummary(): PortfolioSummary
    suspend fun getAssetAllocation(): AssetAllocation
    suspend fun getTransactions(): List<MutualFundTransaction>
    // --- NEW: Add these methods to the interface ---
    suspend fun getFundDetails(fundId: String): MutualFundHolding
    suspend fun getFunds(): List<Fund>
    // --- END NEW ---
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
        // For now, returning dummy data as there's no API for this yet
        return listOf(
            MenuItem(id = "profile", title = "My Profile"),
            MenuItem(id = "settings", title = "Settings"),
            MenuItem(id = "statements", title = "Statements"),
            MenuItem(id = "contact", title = "Contact Us"),
            MenuItem(id = "about", title = "About App"),
            MenuItem(id = "logout", title = "Logout")
        )
    }

    override suspend fun getPortfolioSummary(): PortfolioSummary {
        return apiService.getPortfolioSummary()
    }

    override suspend fun getAssetAllocation(): AssetAllocation {
        // TODO: Consider updating this to call apiService.getAssetAllocation() once you have a dedicated asset_allocation.json
        // Current logic: Calculates based on holdings fetched from API
        val holdings = getHoldings() // Use the non-deprecated getHoldings()
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
        // --- FIXED: Now calling API service instead of returning dummy data ---
        return apiService.getTransactions()
        // --- END FIXED ---
    }

    // --- NEW: Implementations for newly added interface methods ---
    override suspend fun getFundDetails(fundId: String): MutualFundHolding {
        return apiService.getFundDetails(fundId)
    }

    override suspend fun getFunds(): List<Fund> {
        return apiService.getFunds()
    }
    // --- END NEW ---
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

    private val dummyPortfolioSummary = PortfolioSummary(
        totalInvested = 150000.0,
        currentValue = 165000.0,
        overallGainLoss = 15000.0,
        overallGainLossPercentage = 10.0,
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

    // NEW: Dummy funds data for MockMutualFundAppRepository
    private val dummyFunds = listOf(
        Fund(
            fundName = "Mock Equity Fund",
            isin = "MOCKETF001",
            currentNav = 120.5,
            previousDayNav = 119.8,
            fundType = "Equity",
            category = "Multi Cap",
            riskLevel = "High",
            aum = 10000.0,
            minInvestment = 100.0,
            expenseRatio = 0.8,
            oneYearReturn = 18.0,
            threeYearReturn = 15.0,
            fiveYearReturn = 12.0,
            fundHouse = "Mock AMC"
        ),
        Fund(
            fundName = "Mock Debt Fund",
            isin = "MOCKDBT001",
            currentNav = 105.2,
            previousDayNav = 105.1,
            fundType = "Debt",
            category = "Short Duration",
            riskLevel = "Low",
            aum = 5000.0,
            minInvestment = 500.0,
            expenseRatio = 0.2,
            oneYearReturn = 7.0,
            threeYearReturn = 6.5,
            fiveYearReturn = 6.0,
            fundHouse = "Mock Debt House"
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
            MenuItem(id = "mock_settings", title = "Mock Settings"),
            MenuItem(id = "mock_statements", title = "Mock Statements") // Added for completeness
        )
    }

    override suspend fun getPortfolioSummary(): PortfolioSummary {
        return dummyPortfolioSummary
    }

    override suspend fun getAssetAllocation(): AssetAllocation {
        // Calculate based on dummy holdings
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

    // --- NEW: Implementations for newly added interface methods in Mock repository ---
    override suspend fun getFundDetails(fundId: String): MutualFundHolding {
        // Return a dummy holding, or null if not found
        return dummyHoldings.firstOrNull { it.isin == fundId }
            ?: dummyHoldings.first() // Return first as a fallback if not found
    }

    override suspend fun getFunds(): List<Fund> {
        return dummyFunds
    }
    // --- END NEW ---
}