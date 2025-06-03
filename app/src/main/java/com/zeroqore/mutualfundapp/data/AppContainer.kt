// app/src/main/java/com/zeroqore/mutualfundapp/data/AppContainer.kt
package com.zeroqore.mutualfundapp.data

import android.content.Context
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
// - Fund.kt // ADDED: Reminder for the new Fund.kt file

// Import the repository interface and its concrete implementations
import com.zeroqore.mutualfundapp.network.MutualFundApiService

// ADDED IMPORTS FOR LOGIN MODULE
import com.zeroqore.mutualfundapp.data.auth.LoginRepository
import com.zeroqore.mutualfundapp.data.auth.NetworkLoginRepository
import com.zeroqore.mutualfundapp.data.auth.LocalLoginRepository // ADDED THIS IMPORT
import com.zeroqore.mutualfundapp.network.AuthService
import com.zeroqore.mutualfundapp.network.RetrofitClient


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
            // Check for specific exact filenames first
            url.endsWith("holdings.json") -> "holdings.json"
            url.endsWith("transactions.json") -> "transactions.json"
            url.endsWith("portfolio_summary.json") -> "portfolio_summary.json"
            url.endsWith("funds.json") -> "funds.json" // ADDED: Mapping for funds.json
            // If you have a mock login response in assets, you could map it here:
            // url.endsWith("/api/auth/login") -> "mock_login_success.json" // Example for mock API response

            // Check for dynamic paths like fund_details/{fundId}.json
            pathSegments.size >= 2 && pathSegments[pathSegments.size - 2] == "fund_details" ->
                "fund_details_${pathSegments.last()}"

            // If a previous asset was for "mutual_fund_holdings.json" but now it's "holdings.json"
            // You can keep this if you still have old API calls using that path, otherwise remove.
            url.endsWith("mutual_fund_holdings.json") -> "holdings.json" // Consider removing if not used

            else -> null
        }

        if (assetFileName != null) {
            try {
                val jsonString = getJsonFromAssets(assetFileName)
                println("AssetReadingInterceptor: Serving $assetFileName for URL: $url")

                Response.Builder()
                    .code(200)
                    .message("OK - From Assets")
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .body(jsonString.toResponseBody("application/json".toMediaTypeOrNull()))
                    .addHeader("content-type", "application/json")
                    .build()
            } catch (e: IOException) {
                println("AssetReadingInterceptor Error: ${e.message}")
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
            println("AssetReadingInterceptor: No asset mock found for URL: $url. Proceeding to network.")
            chain.proceed(request)
        }
    }

    // UPDATED: okHttpClient now conditionally adds assetReadingInterceptor
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .apply {
            if (useMockAssetInterceptor) { // ADDED: Conditionally add interceptor
                addInterceptor(assetReadingInterceptor)
            }
            // Add any real API interceptors (e.g., AuthInterceptor) here if not using mock
            // else { addInterceptor(AuthInterceptor()) }
        }
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl) // Uses the BASE_URL passed in constructor
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val mutualFundApiService: MutualFundApiService by lazy {
        retrofit.create(MutualFundApiService::class.java)
    }

    // ADDED: AuthService instance from RetrofitClient for login
    // Note: If RetrofitClient creates AuthService internally without needing 'retrofit' directly,
    // this line might remain as is. Otherwise, you'd create it via 'retrofit.create(AuthService::class.java)'
    private val authService: AuthService by lazy {
        RetrofitClient.authService
    }

    // UPDATED: mutualFundRepository now conditionally provides Network or Mock repository
    val mutualFundRepository: MutualFundAppRepository by lazy {
        if (useMockAssetInterceptor) {
            // When using mock assets, we still use the NetworkMutualFundAppRepository
            // because the AssetReadingInterceptor will handle the mock responses.
            // If you wanted a completely separate, hardcoded mock path for the repository,
            // you could use MockMutualFundAppRepository here, but it requires aligning its dummy data.
            // For now, let's assume USE_MOCK_ASSET_INTERCEPTOR means 'mock via assets'.
            NetworkMutualFundAppRepository(mutualFundApiService)
        } else {
            // For real API calls, provide the network-backed repository
            NetworkMutualFundAppRepository(mutualFundApiService)
        }
        // If you had a different flag for 'fully hardcoded mock repo' vs 'network with asset interceptor',
        // you would put that logic here. E.g.,
        // if (useFullHardcodedMockRepo) {
        //     MockMutualFundAppRepository()
        // } else if (useMockAssetInterceptor) {
        //     NetworkMutualFundAppRepository(mutualFundApiService) // Interceptor handles mock
        // } else {
        //     NetworkMutualFundAppRepository(mutualFundApiService) // Real network
        // }
    }

    // MODIFIED: LoginRepository instance now conditionally uses Local or Network repository
    val loginRepository: LoginRepository by lazy {
        if (useMockAssetInterceptor) { // Use the same flag to enable local login
            LocalLoginRepository()
        } else {
            NetworkLoginRepository(authService)
        }
    }
}