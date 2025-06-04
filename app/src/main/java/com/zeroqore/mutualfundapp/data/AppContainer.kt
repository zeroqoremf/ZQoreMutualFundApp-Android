// app/src/main/java/com/zeroqore/mutualfundapp/data/AppContainer.kt
package com.zeroqore.mutualfundapp.data

import android.content.Context
import android.util.Log // ADDED: Import for Log class
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

// Important: Ensure the following data classes are in their own respective .kt files:
// - MutualFundHolding.kt
// - PortfolioSummary.kt
// - AssetAllocation.kt
// - MutualFundTransaction.kt
// - MenuItem.kt
// - Fund.kt

// Import the repository interface and its concrete implementations
import com.zeroqore.mutualfundapp.network.MutualFundApiService

// ADDED IMPORTS FOR LOGIN MODULE
import com.zeroqore.mutualfundapp.data.auth.LoginRepository
import com.zeroqore.mutualfundapp.data.auth.NetworkLoginRepository
import com.zeroqore.mutualfundapp.data.auth.LocalLoginRepository
import com.zeroqore.mutualfundapp.network.AuthService
import com.zeroqore.mutualfundapp.network.RetrofitClient
import com.zeroqore.mutualfundapp.network.AuthInterceptor

// UPDATED: AppContainer now takes base URL and mock flag from BuildConfig
class AppContainer(
    private val context: Context,
    private val baseUrl: String, // Base URL from BuildConfig
    private val useMockAssetInterceptor: Boolean // Flag from BuildConfig
) {

    // Setup HttpLoggingInterceptor for logging network requests and responses
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Logs request and response bodies
    }

    // HELPER FUNCTION: To read a file from assets
    private fun getJsonFromAssets(fileName: String): String {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            throw IOException("Error reading asset file: $fileName. ${e.message}", e)
        }
    }

    // UPDATED: Custom Interceptor to read from assets based on the requested URL path
    private val assetReadingInterceptor = Interceptor { chain ->
        val request = chain.request()
        val url = request.url.toString()
        val pathSegments = request.url.pathSegments

        val assetFileName: String? = when {
            // NEW RULE: Handle dynamic holdings path for mock data
            url.contains("/api/distributors/") && url.contains("/investors/") && url.endsWith("/holdings") -> {
                Log.d("AssetInterceptor", "Matched dynamic holdings path. Serving holdings.json") // CHANGED: println to Log.d
                "holdings.json"
            }
            // ADDED: Handle dynamic portfolio-summary path for mock data
            url.contains("/api/distributors/") && url.contains("/investors/") && url.endsWith("/portfolio-summary") -> {
                Log.d("AssetInterceptor", "Matched dynamic portfolio summary path. Serving portfolio_summary.json") // CHANGED: println to Log.d
                "portfolio_summary.json"
            }
            // ADDED: Handle dynamic transactions path for mock data -- THIS IS THE NEW ADDITION
            url.contains("/api/distributors/") && url.contains("/investors/") && url.endsWith("/transactions") -> {
                Log.d("AssetInterceptor", "Matched dynamic transactions path. Serving transactions.json") // CHANGED: println to Log.d
                "transactions.json"
            }
            // Existing rules (can be kept if other parts of the app still use these direct paths,
            // but the dynamic rules handle the API calls more robustly)
            url.endsWith("holdings.json") -> "holdings.json"
            url.endsWith("transactions.json") -> "transactions.json"
            url.endsWith("portfolio_summary.json") -> "portfolio_summary.json"
            url.endsWith("funds.json") -> "funds.json"

            // Check for dynamic paths like fund_details/{fundId}.json
            pathSegments.size >= 2 && pathSegments[pathSegments.size - 2] == "fund_details" ->
                "fund_details_${pathSegments.last()}"

            // If a previous asset was for "mutual_fund_holdings.json" but now it's "holdings.json"
            url.endsWith("mutual_fund_holdings.json") -> "holdings.json"

            else -> null
        }

        if (assetFileName != null) {
            try {
                val jsonString = getJsonFromAssets(assetFileName)
                Log.d("AssetInterceptor", "Serving $assetFileName for URL: $url") // CHANGED: println to Log.d

                Response.Builder()
                    .code(200)
                    .message("OK - From Assets")
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .body(jsonString.toResponseBody("application/json".toMediaTypeOrNull()))
                    .addHeader("content-type", "application/json")
                    .build()
            } catch (e: IOException) {
                Log.e("AssetInterceptor", "Error reading asset file: $assetFileName. ${e.message}", e) // CHANGED: println to Log.e
                Response.Builder()
                    .code(404)
                    .message("Asset mock file not found or could not be read: $assetFileName")
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .body("{}".toResponseBody("application/json".toMediaTypeOrNull()))
                    .addHeader("content-type", "application/json")
                    .build()
            }
        } else {
            Log.d("AssetInterceptor", "No asset mock found for URL: $url. Proceeding to network.") // CHANGED: println to Log.d
            chain.proceed(request)
        }
    }

    // AuthTokenManager instance
    val authTokenManager: AuthTokenManager by lazy {
        AuthTokenManager(context)
    }

    // AuthInterceptor instance
    private val authInterceptor: AuthInterceptor by lazy {
        AuthInterceptor(authTokenManager)
    }

    // okHttpClient now conditionally adds assetReadingInterceptor AND AuthInterceptor
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .apply {
            if (useMockAssetInterceptor) {
                addInterceptor(assetReadingInterceptor)
            } else {
                addInterceptor(authInterceptor)
            }
        }
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val mutualFundApiService: MutualFundApiService by lazy {
        retrofit.create(MutualFundApiService::class.java)
    }

    // AuthService instance from RetrofitClient for login
    private val authService: AuthService by lazy {
        RetrofitClient.authService
    }

    // UPDATED: mutualFundRepository now conditionally provides Network or Mock repository
    val mutualFundRepository: MutualFundAppRepository by lazy {
        if (useMockAssetInterceptor) {
            // MODIFIED: Pass authTokenManager to NetworkMutualFundAppRepository
            NetworkMutualFundAppRepository(mutualFundApiService, authTokenManager) // Pass authTokenManager here too
        } else {
            NetworkMutualFundAppRepository(mutualFundApiService, authTokenManager) // And here for real API
        }
    }

    // LoginRepository instance now conditionally uses Local or Network repository
    val loginRepository: LoginRepository by lazy {
        if (useMockAssetInterceptor) {
            LocalLoginRepository()
        } else {
            NetworkLoginRepository(authService)
        }
    }
}