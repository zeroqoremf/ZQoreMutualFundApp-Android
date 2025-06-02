// app/src/main/java/com/zeroqore/mutualfundapp/network/MutualFundApiService.kt
package com.zeroqore.mutualfundapp.network

import com.zeroqore.mutualfundapp.data.MutualFundHolding
import com.zeroqore.mutualfundapp.data.MutualFundTransaction
import com.zeroqore.mutualfundapp.data.PortfolioSummary
import com.zeroqore.mutualfundapp.data.Fund // <-- ADDED THIS IMPORT

import retrofit2.http.GET
import retrofit2.http.Path

interface MutualFundApiService {

    @Deprecated("Use getHoldings() instead for a more modular approach.")
    @GET("holdings.json")
    suspend fun getFundHoldings(): List<MutualFundHolding>

    @GET("holdings.json")
    suspend fun getHoldings(): List<MutualFundHolding>

    @GET("transactions.json")
    suspend fun getTransactions(): List<MutualFundTransaction>

    // FIX: Changed return type from Map<String, Any> to PortfolioSummary
    @GET("portfolio_summary.json")
    suspend fun getPortfolioSummary(): PortfolioSummary

    @GET("fund_details/{fundId}.json")
    suspend fun getFundDetails(@Path("fundId") fundId: String): MutualFundHolding

    @GET("funds.json")
    suspend fun getFunds(): List<Fund> // <-- ADDED THIS METHOD

}