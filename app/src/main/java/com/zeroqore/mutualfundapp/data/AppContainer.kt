// app/src/main/java/com/zeroqore.mutualfundapp/data/AppContainer.kt
package com.zeroqore.mutualfundapp.data

import android.content.Context
import android.util.Log
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
import java.util.concurrent.TimeUnit

// Import the repository interface and its concrete implementations
import com.zeroqore.mutualfundapp.network.MutualFundApiService

// ADDED IMPORTS FOR LOGIN MODULE
import com.zeroqore.mutualfundapp.data.auth.LoginRepository
import com.zeroqore.mutualfundapp.data.auth.NetworkLoginRepository
import com.zeroqore.mutualfundapp.data.auth.LocalLoginRepository
import com.zeroqore.mutualfundapp.network.AuthService
import com.zeroqore.mutualfundapp.network.RetrofitClient // This is still used for authService
import com.zeroqore.mutualfundapp.network.AuthInterceptor

// UPDATED: AppContainer now takes base URL and granular mock flags from BuildConfig
class AppContainer(
    private val context: Context,
    private val baseUrl: String,
    private val useLiveLoginApi: Boolean,
    private val useDashboardMocks: Boolean
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
            // Updated to match the /api/holdings, /api/transactions, /api/portfolio-summary patterns
            // from your MutualFundApiService.kt if it's hitting live.
            // If the service is using `holdings.json` directly as I suggested in the previous response
            // to align with asset files, then these first three patterns won't be matched.
            // For clarity, I'm adapting it to match the common /api/X structure
            // However, remember: If you choose to use `MockMutualFundAppRepository`,
            // this interceptor is NOT used for dashboard data.
            url.contains("/api/holdings") -> "holdings.json" // Changed to match common API paths
            url.contains("/api/transactions") -> "transactions.json"
            url.contains("/api/portfolio-summary") -> "portfolio_summary.json"

            // Existing rules (can be kept if other parts of the app still use these direct paths)
            url.endsWith("holdings.json") -> "holdings.json"
            url.endsWith("transactions.json") -> "transactions.json"
            url.endsWith("portfolio_summary.json") -> "portfolio_summary.json"
            url.endsWith("funds.json") -> "funds.json"

            // Check for dynamic paths like fund_details/{fundId}.json
            pathSegments.size >= 2 && pathSegments[pathSegments.size - 2] == "fund_details" ->
                "fund_details_${pathSegments.last()}"

            // This seems redundant with holdings.json above, but keeping it if there's a specific use-case
            url.endsWith("mutual_fund_holdings.json") -> "holdings.json"

            else -> null
        }

        if (assetFileName != null) {
            try {
                val jsonString = getJsonFromAssets(assetFileName)
                Log.d("AssetInterceptor", "Serving $assetFileName for URL: $url")

                Response.Builder()
                    .code(200)
                    .message("OK - From Assets")
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .body(jsonString.toResponseBody("application/json".toMediaTypeOrNull()))
                    .addHeader("content-type", "application/json")
                    .build()
            } catch (e: IOException) {
                Log.e("AssetInterceptor", "Error reading asset file: $assetFileName. ${e.message}", e)
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
            Log.d("AssetInterceptor", "No asset mock found for URL: $url. Proceeding to network.")
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

    // okHttpClient now uses authInterceptor always (for authenticated calls) and assetReadingInterceptor conditionally
    // The assetReadingInterceptor is primarily for scenarios where you want to mock *some* network calls
    // even when using the NetworkMutualFundAppRepository.
    // However, for the dashboard, we're explicitly switching the entire repository.
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        // This assetReadingInterceptor is for when you *do* use NetworkMutualFundAppRepository
        // but want certain paths to be mocked by assets.
        // It's still good to keep it here if other parts of the app rely on it.
        // For dashboard calls, we're now bypassing this entire OkHttpClient setup if useDashboardMocks is true.
        .apply {
            // This is important: if you have BuildConfig.USE_MOCK_ASSET_INTERCEPTOR = true,
            // then this interceptor should be added.
            // Your gradle file implies USE_MOCK_ASSET_INTERCEPTOR = true along with USE_DASHBOARD_MOCKS.
            // If this interceptor is generally intended for *any* asset mocking (not just dashboard),
            // then its condition should probably be `BuildConfig.USE_MOCK_ASSET_INTERCEPTOR`
            // rather than `useDashboardMocks`.
            // For now, I'm keeping it as you had it, tied to `useDashboardMocks` for consistency,
            // but remember this interceptor won't be hit for dashboard calls if MockMutualFundAppRepository is used.
            if (useDashboardMocks) { // Or BuildConfig.USE_MOCK_ASSET_INTERCEPTOR
                addInterceptor(assetReadingInterceptor)
            }
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
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
        RetrofitClient.authService // This uses RetrofitClient's own OkHttpClient
    }

    // LoginRepository instance now conditionally uses Local or Network repository
    val loginRepository: LoginRepository by lazy {
        if (useLiveLoginApi) {
            NetworkLoginRepository(authService)
        } else {
            LocalLoginRepository()
        }
    }

    // --- CRITICAL FIX HERE ---
    // mutualFundRepository now correctly provides Network or Mock repository based on useDashboardMocks
    val mutualFundRepository: MutualFundAppRepository by lazy {
        if (useDashboardMocks) {
            Log.d("AppContainer", "Providing MockMutualFundAppRepository for dashboard data.")
            MockMutualFundAppRepository() // CORRECTED: Provide the Mock implementation
        } else {
            Log.d("AppContainer", "Providing NetworkMutualFundAppRepository for dashboard data.")
            NetworkMutualFundAppRepository(mutualFundApiService, authTokenManager) // Provide the real network implementation
        }
    }
}