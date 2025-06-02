// app/src/main/java/com/zeroqore/mutualfundapp/data/MutualFundAppRepository.kt
package com.zeroqore.mutualfundapp.data

import com.zeroqore.mutualfundapp.network.MutualFundApiService
import com.zeroqore.mutualfundapp.util.Results // <<< CHANGED IMPORT PATH AND NAME
import retrofit2.HttpException
import java.io.IOException

// Renamed Interface for the Mutual Fund Repository
interface MutualFundAppRepository {
    // Keep for now, but consider removing later as it's deprecated in API service
    suspend fun getFundHoldings(): Results<List<MutualFundHolding>>
    suspend fun getHoldings(): Results<List<MutualFundHolding>>
    suspend fun getMenuItems(): List<MenuItem>
    suspend fun getPortfolioSummary(): Results<PortfolioSummary>
    suspend fun getAssetAllocation(): AssetAllocation
    suspend fun getTransactions(): Results<List<MutualFundTransaction>>
    // --- NEW: Add these methods to the interface ---
    suspend fun getFundDetails(fundId: String): Results<MutualFundHolding>
    suspend fun getFunds(): Results<List<Fund>>
    // --- END NEW ---
}

// Renamed Concrete implementation of the Repository that uses the network service
class NetworkMutualFundAppRepository(private val apiService: MutualFundApiService) : MutualFundAppRepository {

    override suspend fun getFundHoldings(): Results<List<MutualFundHolding>> {
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
            val holdings = apiService.getHoldings()
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
        // For now, returning dummy data as there's no API for this yet
        // No change needed here as it doesn't make an API call
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
            val summary = apiService.getPortfolioSummary()
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
        // This method calculates based on holdings, not a direct API call.
        // However, getHoldings() now returns a Results, so we need to handle that.
        val holdingsResult = getHoldings() // This now returns Results<List<MutualFundHolding>>

        return when (holdingsResult) {
            is Results.Success -> {
                val holdings = holdingsResult.data // Get the actual list of holdings
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
                // If holdings couldn't be fetched, return an AssetAllocation with zero values
                // You might also want to log this error or report it to a crashlytics service
                AssetAllocation(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
            }
            Results.Loading -> {
                // Should ideally not happen if getHoldings() is awaited, but handled for completeness
                AssetAllocation(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
            }
        }
    }

    override suspend fun getTransactions(): Results<List<MutualFundTransaction>> {
        return try {
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

    // --- Implementations for newly added interface methods ---
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
            purchasePrice = 28000.0, // Added purchasePrice
            units = 150.0,
            currentNav = 233.33, // Corrected from 'nav'
            purchaseNav = 186.67, // Added purchaseNav (example value)
            lastUpdated = "2024-05-30", // Added lastUpdated
            fundType = "Equity",
            category = "Large Cap",
            riskLevel = "High",
            previousDayNav = 230.0 // Added previousDayNav
        ),
        MutualFundHolding(
            fundName = "ICICI Prudential Bluechip Fund",
            isin = "INF109K01K70",
            currentValue = 28000.0,
            purchasePrice = 25000.0, // Added purchasePrice
            units = 120.0,
            currentNav = 233.33, // Corrected from 'nav'
            purchaseNav = 208.33, // Added purchaseNav (example value)
            lastUpdated = "2024-05-30", // Added lastUpdated
            fundType = "Equity",
            category = "Large Cap",
            riskLevel = "High",
            previousDayNav = 231.0 // Added previousDayNav
        ),
        MutualFundHolding(
            fundName = "HDFC Short Term Debt Fund",
            isin = "INF179KC17G0",
            currentValue = 18000.0,
            purchasePrice = 17500.0, // Added purchasePrice
            units = 800.0,
            currentNav = 22.50, // Corrected from 'nav'
            purchaseNav = 21.88, // Added purchaseNav (example value)
            lastUpdated = "2024-05-30", // Added lastUpdated
            fundType = "Debt",
            category = "Short Duration",
            riskLevel = "Low",
            previousDayNav = 22.45 // Added previousDayNav
        ),
        MutualFundHolding(
            fundName = "SBI Conservative Hybrid Fund",
            isin = "INF200K019B8",
            currentValue = 22000.0,
            purchasePrice = 20000.0, // Added purchasePrice
            units = 300.0,
            currentNav = 73.33, // Corrected from 'nav'
            purchaseNav = 66.67, // Added purchaseNav (example value)
            lastUpdated = "2024-05-30", // Added lastUpdated
            fundType = "Hybrid",
            category = "Conservative",
            riskLevel = "Medium",
            previousDayNav = 73.00 // Added previousDayNav
        ),
        MutualFundHolding(
            fundName = "Axis Long Term Equity Fund",
            isin = "INF846K01D60",
            currentValue = 40000.0,
            purchasePrice = 35000.0, // Added purchasePrice
            units = 200.0,
            currentNav = 200.0, // Corrected from 'nav'
            purchaseNav = 175.0, // Added purchaseNav (example value)
            lastUpdated = "2024-05-30", // Added lastUpdated
            fundType = "Equity",
            category = "ELSS",
            riskLevel = "High",
            previousDayNav = 198.0 // Added previousDayNav
        )
    )

    // --- VERIFYING/CORRECTING OTHER DUMMY DATA TO MATCH THEIR DATA CLASSES ---

    // Assuming PortfolioSummary has: totalInvested, currentValue, overallGainLoss, overallGainLossPercentage, lastUpdated
    // Based on your previous PortfolioSummary.kt, it had: totalInvested, currentValue, overallGainLoss, overallGainLossPercentage, lastUpdated
    // The dummy data you provided previously was: totalInvested = 150000.0, currentValue = 165000.0, overallGainLoss = 15000.0, overallGainLossPercentage = 10.0, lastUpdated = "2024-06-01T14:30:00Z"
    // The latest dummy data I provided had: totalInvestedValue, totalCurrentValue, totalGainLoss, totalGainLossPercentage
    // Let's align with your PortfolioSummary.kt (totalInvested, currentValue, overallGainLoss, overallGainLossPercentage, lastUpdated)
    private val dummyPortfolioSummary = PortfolioSummary(
        totalInvested = 150000.0,
        currentValue = 165000.0,
        overallGainLoss = 15000.0,
        overallGainLossPercentage = 10.0,
        lastUpdated = "2024-06-01T14:30:00Z" // Example timestamp
    )

    // Assuming MutualFundTransaction has: transactionId, fundName, isin, transactionDate, transactionType, amount, units, navAtTransaction
    // Your previous dummy data had: transactionId, fundName, type, date, amount, units, nav
    // Let's align with MutualFundTransaction.kt
    private val dummyTransactions = listOf(
        MutualFundTransaction(
            transactionId = "TRN001",
            fundName = "Aditya Birla Sun Life Frontline Equity Fund",
            isin = "INF209K01234", // Added ISIN
            transactionDate = "2023-01-15", // Corrected from 'date'
            transactionType = "BUY", // Corrected from 'type'
            amount = 10000.0,
            units = 40.0,
            navAtTransaction = 250.0 // Corrected from 'nav'
        ),
        MutualFundTransaction(
            transactionId = "TRN002",
            fundName = "HDFC Short Term Debt Fund",
            isin = "INF179KC17G0", // Added ISIN
            transactionDate = "2023-03-20", // Corrected from 'date'
            transactionType = "SELL", // Corrected from 'type'
            amount = 5000.0,
            units = 250.0,
            navAtTransaction = 20.0 // Corrected from 'nav'
        ),
        MutualFundTransaction(
            transactionId = "TRN003",
            fundName = "SBI Conservative Hybrid Fund",
            isin = "INF200K019B8", // Added ISIN
            transactionDate = "2023-04-01", // Corrected from 'date'
            transactionType = "SWP", // Corrected from 'type'
            amount = 1000.0,
            units = 13.63,
            navAtTransaction = 73.33 // Corrected from 'nav'
        )
    )

    // Assuming Fund has: fundName, isin, currentNav, previousDayNav, fundType, category, riskLevel, aum, minInvestment, expenseRatio, oneYearReturn, threeYearReturn, fiveYearReturn, fundHouse
    // Your previous dummy data had: id, name, type, category, riskLevel, returnsLast1Year, returnsLast3Year, returnsLast5Year, minInvestment, expenseRatio, fundManager, exitLoad
    // Let's align with Fund.kt
    private val dummyFunds = listOf(
        Fund(
            fundName = "Large Cap Equity Fund", // Corrected from 'name'
            isin = "FND001EQ", // Added ISIN
            currentNav = 120.5, // Added currentNav
            previousDayNav = 119.8, // Added previousDayNav
            fundType = "Equity", // Corrected from 'type'
            category = "Large Cap",
            riskLevel = "High",
            aum = 100000000.0, // Added AUM (example value)
            minInvestment = 5000.0,
            expenseRatio = 0.8,
            oneYearReturn = 25.5, // Corrected from 'returnsLast1Year'
            threeYearReturn = 18.2, // Corrected from 'returnsLast3Year'
            fiveYearReturn = 15.0, // Corrected from 'returnsLast5Year'
            fundHouse = "ABC Asset Management" // Corrected from 'fundManager', Added fundHouse
        ),
        Fund(
            fundName = "Liquid Debt Fund", // Corrected from 'name'
            isin = "FND002DB", // Added ISIN
            currentNav = 105.2, // Added currentNav
            previousDayNav = 105.1, // Added previousDayNav
            fundType = "Debt", // Corrected from 'type'
            category = "Liquid",
            riskLevel = "Low",
            aum = 50000000.0, // Added AUM (example value)
            minInvestment = 1000.0,
            expenseRatio = 0.2,
            oneYearReturn = 6.8, // Corrected from 'returnsLast1Year'
            threeYearReturn = 6.2, // Corrected from 'returnsLast3Year'
            fiveYearReturn = 5.9, // Corrected from 'returnsLast5Year'
            fundHouse = "XYZ Debt House" // Corrected from 'fundManager', Added fundHouse
        ),
        Fund(
            fundName = "Aggressive Hybrid Fund", // Corrected from 'name'
            isin = "FND003HB", // Added ISIN
            currentNav = 180.0, // Added currentNav
            previousDayNav = 178.5, // Added previousDayNav
            fundType = "Hybrid", // Corrected from 'type'
            category = "Aggressive",
            riskLevel = "Medium to High",
            aum = 75000000.0, // Added AUM (example value)
            minInvestment = 2000.0,
            expenseRatio = 0.5,
            oneYearReturn = 18.0, // Corrected from 'returnsLast1Year'
            threeYearReturn = 14.5, // Corrected from 'returnsLast3Year'
            fiveYearReturn = 12.8, // Corrected from 'returnsLast5Year'
            fundHouse = "PQR Wealth Management" // Corrected from 'fundManager', Added fundHouse
        )
    )
    // --- END POPULATED DUMMY DATA ---

    override suspend fun getFundHoldings(): Results<List<MutualFundHolding>> {
        return Results.Success(dummyHoldings)
    }

    override suspend fun getHoldings(): Results<List<MutualFundHolding>> {
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
        return Results.Success(dummyPortfolioSummary)
    }

    override suspend fun getAssetAllocation(): AssetAllocation {
        // This will still calculate based on holdings, but you need to get them from a Results
        val holdingsResult = getHoldings() // This now returns Results<List<MutualFundHolding>>

        return when (holdingsResult) {
            is Results.Success -> {
                val holdings = holdingsResult.data // Get the actual list of holdings
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
                // If holdings couldn't be fetched, return an AssetAllocation with zero values
                // You might also want to log this error or report it to a crashlytics service
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