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

    @GET("api/distributors/{distributorId}/investors/{investorId}/holdings")
    suspend fun getHoldings(
        @Path("distributorId") distributorId: String,
        @Path("investorId") investorId: String
    ): List<MutualFundHolding>

    @GET("transactions.json")
    suspend fun getTransactions(): List<MutualFundTransaction>

    // MODIFIED: Updated @GET path and added path parameters for PortfolioSummary
    @GET("api/distributors/{distributorId}/investors/{investorId}/portfolio-summary")
    suspend fun getPortfolioSummary(
        @Path("distributorId") distributorId: String,
        @Path("investorId") investorId: String
    ): PortfolioSummary

    @GET("fund_details/{fundId}.json")
    suspend fun getFundDetails(@Path("fundId") fundId: String): MutualFundHolding

    @GET("funds.json")
    suspend fun getFunds(): List<Fund>

}