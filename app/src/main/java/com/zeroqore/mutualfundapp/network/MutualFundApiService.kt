// app/src/main/java/com/zeroqore.mutualfundapp/network/MutualFundApiService.kt
package com.zeroqore.mutualfundapp.network

import com.zeroqore.mutualfundapp.data.MutualFundHolding
import com.zeroqore.mutualfundapp.data.MutualFundTransaction
import com.zeroqore.mutualfundapp.data.PortfolioSummary
import com.zeroqore.mutualfundapp.data.Fund

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query // IMPORTED: We now need Query for optional parameters

interface MutualFundApiService {

    @Deprecated("Use getHoldings(investorId: String, distributorId: String?) instead for a more modular approach.")
    @GET("holdings.json")
    suspend fun getFundHoldings(): List<MutualFundHolding>

    // MODIFIED: Changed from @Path to @Query for both investorId and distributorId
    // The endpoint path should now just be the base for holdings, as parameters are in query string
    @GET("api/holdings") // Example: Adjust this path to your actual endpoint that accepts query parameters
    suspend fun getHoldings(
        @Query("distributorId") distributorId: String?, // Now nullable String?
        @Query("investorId") investorId: String? // Also changed to nullable String? for consistency, if your API supports it.
        // If investorId is ALWAYS required, keep it String
    ): List<MutualFundHolding>

    // MODIFIED: Changed from @Path to @Query for both investorId and distributorId
    @GET("api/transactions") // Example: Adjust this path to your actual endpoint
    suspend fun getTransactions(
        @Query("distributorId") distributorId: String?, // Now nullable String?
        @Query("investorId") investorId: String? // Also changed to nullable String?
    ): List<MutualFundTransaction>

    @Deprecated("Use getTransactions(investorId: String, distributorId: String?) instead.")
    @GET("transactions.json")
    suspend fun getTransactionsSimplified(): List<MutualFundTransaction>


    // MODIFIED: Changed from @Path to @Query for both investorId and distributorId
    @GET("api/portfolio-summary") // Example: Adjust this path to your actual endpoint
    suspend fun getPortfolioSummary(
        @Query("distributorId") distributorId: String?, // Now nullable String?
        @Query("investorId") investorId: String? // Also changed to nullable String?
    ): PortfolioSummary

    // This one looks fine as fundId is typically always required and part of the path
    @GET("fund_details/{fundId}.json")
    suspend fun getFundDetails(@Path("fundId") fundId: String): MutualFundHolding

    @GET("funds.json")
    suspend fun getFunds(): List<Fund>

}