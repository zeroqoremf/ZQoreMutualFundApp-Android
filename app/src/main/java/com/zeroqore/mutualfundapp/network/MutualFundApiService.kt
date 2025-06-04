// app/src/main/java/com/zeroqore/mutualfundapp/network/MutualFundApiService.kt
package com.zeroqore.mutualfundapp.network

import com.zeroqore.mutualfundapp.data.MutualFundHolding
import com.zeroqore.mutualfundapp.data.MutualFundTransaction
import com.zeroqore.mutualfundapp.data.PortfolioSummary
import com.zeroqore.mutualfundapp.data.Fund

import retrofit2.http.GET
import retrofit2.http.Path

interface MutualFundApiService {

    @Deprecated("Use getHoldings(investorId: String, distributorId: String) instead for a more modular approach.")
    @GET("holdings.json")
    suspend fun getFundHoldings(): List<MutualFundHolding>

    @GET("api/distributors/{distributorId}/investors/{investorId}/holdings") // MODIFIED: Added distributorId path parameter
    suspend fun getHoldings(
        @Path("distributorId") distributorId: String, // ADDED: distributorId parameter
        @Path("investorId") investorId: String
    ): List<MutualFundHolding>

    @GET("transactions.json")
    suspend fun getTransactions(): List<MutualFundTransaction>

    @GET("portfolio_summary.json")
    suspend fun getPortfolioSummary(): PortfolioSummary

    @GET("fund_details/{fundId}.json")
    suspend fun getFundDetails(@Path("fundId") fundId: String): MutualFundHolding

    @GET("funds.json")
    suspend fun getFunds(): List<Fund>

}