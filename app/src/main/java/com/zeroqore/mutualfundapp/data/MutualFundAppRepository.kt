// app/src/main/java/com/zeroqore/mutualfundapp/data/MutualFundAppRepository.kt
package com.zeroqore.mutualfundapp.data

import com.zeroqore.mutualfundapp.network.MutualFundApiService
import com.zeroqore.mutualfundapp.util.Results
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException

// Renamed Interface for the Mutual Fund Repository
interface MutualFundAppRepository {
    // Keep for now, but consider removing later as it's deprecated in API service
    suspend fun getFundHoldings(): Results<List<MutualFundHolding>>
    // MODIFIED: Updated signature to reflect changes in MutualFundApiService
    suspend fun getHoldings(): Results<List<MutualFundHolding>> // Interface method remains the same
    suspend fun getMenuItems(): List<MenuItem>
    // MODIFIED: Interface method remains the same, but implementation will now use IDs
    suspend fun getPortfolioSummary(): Results<PortfolioSummary>
    // Asset allocation will still operate on the Results of holdings
    suspend fun getAssetAllocation(): AssetAllocation
    suspend fun getTransactions(): Results<List<MutualFundTransaction>>
    suspend fun getFundDetails(fundId: String): Results<MutualFundHolding>
    suspend fun getFunds(): Results<List<Fund>>
}

// Renamed Concrete implementation of the Repository that uses the network service
class NetworkMutualFundAppRepository(
    private val apiService: MutualFundApiService,
    // ADDED: Inject AuthTokenManager here
    private val authTokenManager: AuthTokenManager
) : MutualFundAppRepository {

    override suspend fun getFundHoldings(): Results<List<MutualFundHolding>> {
        // This method is deprecated in API service and will be removed later.
        // It does not use investorId/distributorId.
        return try {
            val holdings = apiService.getFundHoldings()
            Results.Success(holdings)
        } catch (e: IOException) {
            Results.Error(e, "Please check your internet connection for fund holdings.")
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                404 -> "Holdings data not found. Please try again later."
                in 400..499 -> "Client error fetching holdings: ${e.message()}"
                in 500..599 -> "Server error fetching holdings. Please try again later."
                else -> "An unexpected error occurred: ${e.message()}"
            }
            Results.Error(e, errorMessage)
        } catch (e: Exception) {
            Results.Error(e, "An unknown error occurred while fetching fund holdings.")
        }
    }

    override suspend fun getHoldings(): Results<List<MutualFundHolding>> {
        return try {
            val investorId = authTokenManager.getInvestorId()
            val distributorId = authTokenManager.getDistributorId()

            if (investorId == null || distributorId == null) {
                return Results.Error(
                    IllegalStateException("Investor ID or Distributor ID not found."),
                    "User not logged in or authentication data missing. Please log in again."
                )
            }

            // MODIFIED: Pass both distributorId and investorId to the API service
            val holdings = apiService.getHoldings(distributorId, investorId)
            Results.Success(holdings)
        } catch (e: IOException) {
            Results.Error(e, "Please check your internet connection for holdings.")
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                404 -> "Holdings data not found. Please try again later."
                in 400..499 -> "Client error fetching holdings: ${e.message()}"
                in 500..599 -> "Server error fetching holdings. Please try again later."
                else -> "An unexpected error occurred: ${e.message()}"
            }
            Results.Error(e, errorMessage)
        } catch (e: Exception) {
            Results.Error(e, "An unknown error occurred while fetching holdings.")
        }
    }

    override suspend fun getMenuItems(): List<MenuItem> {
        return listOf(
            MenuItem(id = "profile", title = "My Profile"),
            MenuItem(id = "settings", title = "Settings"),
            MenuItem(id = "statements", title = "Statements"),
            MenuItem(id = "contact", title = "Contact Us"),
            MenuItem(id = "about", title = "About App"),
            MenuItem(id = "logout", title = "Logout")
        )
    }

    override suspend fun getPortfolioSummary(): Results<PortfolioSummary> {
        return try {
            // ADDED: Retrieve investorId and distributorId
            val investorId = authTokenManager.getInvestorId()
            val distributorId = authTokenManager.getDistributorId()

            if (investorId == null || distributorId == null) {
                return Results.Error(
                    IllegalStateException("Investor ID or Distributor ID not found."),
                    "User not logged in or authentication data missing. Please log in again."
                )
            }

            // MODIFIED: Pass both distributorId and investorId to the API service
            val summary = apiService.getPortfolioSummary(distributorId, investorId)
            Results.Success(summary)
        } catch (e: IOException) {
            Results.Error(e, "Please check your internet connection for portfolio summary.")
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                404 -> "Portfolio summary not found. Please try again later."
                in 400..499 -> "Client error fetching portfolio summary: ${e.message()}"
                in 500..599 -> "Server error fetching portfolio summary. Please try again later."
                else -> "An unexpected error occurred: ${e.message()}"
            }
            Results.Error(e, errorMessage)
        } catch (e: Exception) {
            Results.Error(e, "An unknown error occurred while fetching portfolio summary.")
        }
    }

    override suspend fun getAssetAllocation(): AssetAllocation {
        val holdingsResult = getHoldings()

        return when (holdingsResult) {
            is Results.Success -> {
                val holdings = holdingsResult.data
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

                AssetAllocation(
                    equityValue = equityValue,
                    equityPercentage = equityPercentage,
                    debtValue = debtValue,
                    debtPercentage = debtPercentage,
                    hybridValue = hybridValue,
                    hybridPercentage = hybridPercentage
                )
            }
            is Results.Error -> {
                AssetAllocation(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
            }
            Results.Loading -> {
                AssetAllocation(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
            }
        }
    }

    override suspend fun getTransactions(): Results<List<MutualFundTransaction>> {
        return try {
            // If transactions also needs IDs, you'd add similar logic here
            val transactions = apiService.getTransactions()
            Results.Success(transactions)
        } catch (e: IOException) {
            Results.Error(e, "Please check your internet connection for transactions.")
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                404 -> "Transactions data not found. Please try again later."
                in 400..499 -> "Client error fetching transactions: ${e.message()}"
                in 500..599 -> "Server error fetching transactions. Please try again later."
                else -> "An unexpected error occurred: ${e.message()}"
            }
            Results.Error(e, errorMessage)
        } catch (e: Exception) {
            Results.Error(e, "An unknown error occurred while fetching transactions.")
        }
    }

    override suspend fun getFundDetails(fundId: String): Results<MutualFundHolding> {
        return try {
            val fundDetails = apiService.getFundDetails(fundId)
            Results.Success(fundDetails)
        } catch (e: IOException) {
            Results.Error(e, "Please check your internet connection for fund details.")
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                404 -> "Fund details not found for ID: $fundId."
                in 400..499 -> "Client error fetching fund details: ${e.message()}"
                in 500..599 -> "Server error fetching fund details. Please try again later."
                else -> "An unexpected error occurred: ${e.message()}"
            }
            Results.Error(e, errorMessage)
        } catch (e: Exception) {
            Results.Error(e, "An unknown error occurred while fetching fund details.")
        }
    }

    override suspend fun getFunds(): Results<List<Fund>> {
        return try {
            val funds = apiService.getFunds()
            Results.Success(funds)
        } catch (e: IOException) {
            Results.Error(e, "Please check your internet connection to load funds.")
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                404 -> "No funds data found. Please try again later."
                in 400..499 -> "Client error fetching funds: ${e.message()}"
                in 500..599 -> "Server error fetching funds. Please try again later."
                else -> "An unexpected error occurred: ${e.message()}"
            }
            Results.Error(e, errorMessage)
        } catch (e: Exception) {
            Results.Error(e, "An unknown error occurred while fetching funds.")
        }
    }
}

// Renamed Optional: Mock implementation of the Repository
class MockMutualFundAppRepository : MutualFundAppRepository {
    // --- POPULATED DUMMY DATA (UPDATED TO MATCH MutualFundHolding.kt) ---
    private val dummyHoldings = listOf(
        MutualFundHolding(
            fundName = "Aditya Birla Sun Life Frontline Equity Fund",
            isin = "INF209K01234",
            currentValue = 35000.0,
            purchasePrice = 28000.0,
            units = 150.0,
            currentNav = 233.33,
            purchaseNav = 186.67,
            lastUpdated = "2024-05-30",
            fundType = "Equity",
            category = "Large Cap",
            riskLevel = "High",
            previousDayNav = 230.0
        ),
        MutualFundHolding(
            fundName = "ICICI Prudential Bluechip Fund",
            isin = "INF109K01K70",
            currentValue = 28000.0,
            purchasePrice = 25000.0,
            units = 120.0,
            currentNav = 233.33,
            purchaseNav = 208.33,
            lastUpdated = "2024-05-30",
            fundType = "Equity",
            category = "Large Cap",
            riskLevel = "High",
            previousDayNav = 231.0
        ),
        MutualFundHolding(
            fundName = "HDFC Short Term Debt Fund",
            isin = "INF179KC17G0",
            currentValue = 18000.0,
            purchasePrice = 17500.0,
            units = 800.0,
            currentNav = 22.50,
            purchaseNav = 21.88,
            lastUpdated = "2024-05-30",
            fundType = "Debt",
            category = "Short Duration",
            riskLevel = "Low",
            previousDayNav = 22.45
        ),
        MutualFundHolding(
            fundName = "SBI Conservative Hybrid Fund",
            isin = "INF200K019B8",
            currentValue = 22000.0,
            purchasePrice = 20000.0,
            units = 300.0,
            currentNav = 73.33,
            purchaseNav = 66.67,
            lastUpdated = "2024-05-30",
            fundType = "Hybrid",
            category = "Conservative",
            riskLevel = "Medium",
            previousDayNav = 73.00
        ),
        MutualFundHolding(
            fundName = "Axis Long Term Equity Fund",
            isin = "INF846K01D60",
            currentValue = 40000.0,
            purchasePrice = 35000.0,
            units = 200.0,
            currentNav = 200.0,
            purchaseNav = 175.0,
            lastUpdated = "2024-05-30",
            fundType = "Equity",
            category = "ELSS",
            riskLevel = "High",
            previousDayNav = 198.0
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
            transactionId = "TRN001",
            fundName = "Aditya Birla Sun Life Frontline Equity Fund",
            isin = "INF209K01234",
            transactionDate = "2023-01-15",
            transactionType = "BUY",
            amount = 10000.0,
            units = 40.0,
            navAtTransaction = 250.0
        ),
        MutualFundTransaction(
            transactionId = "TRN002",
            fundName = "HDFC Short Term Debt Fund",
            isin = "INF179KC17G0",
            transactionDate = "2023-03-20",
            transactionType = "SELL",
            amount = 5000.0,
            units = 250.0,
            navAtTransaction = 20.0
        ),
        MutualFundTransaction(
            transactionId = "TRN003",
            fundName = "SBI Conservative Hybrid Fund",
            isin = "INF200K019B8",
            transactionDate = "2023-04-01",
            transactionType = "SWP",
            amount = 1000.0,
            units = 13.63,
            navAtTransaction = 73.33
        )
    )

    private val dummyFunds = listOf(
        Fund(
            fundName = "Large Cap Equity Fund",
            isin = "FND001EQ",
            currentNav = 120.5,
            previousDayNav = 119.8,
            fundType = "Equity",
            category = "Large Cap",
            riskLevel = "High",
            aum = 100000000.0,
            minInvestment = 5000.0,
            expenseRatio = 0.8,
            oneYearReturn = 25.5,
            threeYearReturn = 18.2,
            fiveYearReturn = 15.0,
            fundHouse = "ABC Asset Management"
        ),
        Fund(
            fundName = "Liquid Debt Fund",
            isin = "FND002DB",
            currentNav = 105.2,
            previousDayNav = 105.1,
            fundType = "Debt",
            category = "Liquid",
            riskLevel = "Low",
            aum = 50000000.0,
            minInvestment = 1000.0,
            expenseRatio = 0.2,
            oneYearReturn = 6.8,
            threeYearReturn = 6.2,
            fiveYearReturn = 5.9,
            fundHouse = "XYZ Debt House"
        ),
        Fund(
            fundName = "Aggressive Hybrid Fund",
            isin = "FND003HB",
            currentNav = 180.0,
            previousDayNav = 178.5,
            fundType = "Hybrid",
            category = "Aggressive",
            riskLevel = "Medium to High",
            aum = 75000000.0,
            minInvestment = 2000.0,
            expenseRatio = 0.5,
            oneYearReturn = 18.0,
            threeYearReturn = 14.5,
            fiveYearReturn = 12.8,
            fundHouse = "PQR Wealth Management"
        )
    )

    override suspend fun getFundHoldings(): Results<List<MutualFundHolding>> {
        // This method will be removed. Using the other getHoldings for consistency.
        delay(1500L) // Add a delay here to simulate network call
        return Results.Success(dummyHoldings)
    }

    override suspend fun getHoldings(): Results<List<MutualFundHolding>> {
        delay(1500L) // Add a delay here to simulate network call
        return Results.Success(dummyHoldings)
    }

    override suspend fun getMenuItems(): List<MenuItem> {
        return listOf(
            MenuItem(id = "mock_profile", title = "Mock Profile"),
            MenuItem(id = "mock_settings", title = "Mock Settings"),
            MenuItem(id = "mock_statements", title = "Mock Statements")
        )
    }

    override suspend fun getPortfolioSummary(): Results<PortfolioSummary> {
        delay(1500L) // Add a delay here to simulate network call
        return Results.Success(dummyPortfolioSummary)
    }

    override suspend fun getAssetAllocation(): AssetAllocation {
        val holdingsResult = getHoldings()

        return when (holdingsResult) {
            is Results.Success -> {
                val holdings = holdingsResult.data
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

                AssetAllocation(
                    equityValue = equityValue,
                    equityPercentage = equityPercentage,
                    debtValue = debtValue,
                    debtPercentage = debtPercentage,
                    hybridValue = hybridValue,
                    hybridPercentage = hybridPercentage
                )
            }
            is Results.Error -> {
                AssetAllocation(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
            }
            Results.Loading -> AssetAllocation(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        }
    }

    override suspend fun getTransactions(): Results<List<MutualFundTransaction>> {
        return Results.Success(dummyTransactions)
    }

    override suspend fun getFundDetails(fundId: String): Results<MutualFundHolding> {
        val fund = dummyHoldings.firstOrNull { it.isin == fundId }
        return if (fund != null) {
            Results.Success(fund)
        } else {
            Results.Error(Exception("Mock Fund Details not found for ID: $fundId")) as Results<MutualFundHolding>
        }
    }

    override suspend fun getFunds(): Results<List<Fund>> {
        return Results.Success(dummyFunds)
    }
}