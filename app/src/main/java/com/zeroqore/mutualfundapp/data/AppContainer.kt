// app/src/main/java/com/zeroqore/mutualfundapp/data/AppContainer.kt
package com.zeroqore.mutualfundapp.data

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.zeroqore.mutualfundapp.network.MutualFundApiService
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

// IMPORTANT: Ensure the following data classes are in their own respective .kt files:
// - MutualFundHolding.kt
// - PortfolioSummary.kt
// - AssetAllocation.kt
// - MutualFundTransaction.kt
// - MenuItem.kt

// Import the NEW repository interface and its concrete network implementation
import com.zeroqore.mutualfundapp.data.MutualFundAppRepository
import com.zeroqore.mutualfundapp.data.NetworkMutualFundAppRepository

// AppContainer: Centralizes the creation and provision of application-wide dependencies
@RequiresApi(Build.VERSION_CODES.GINGERBREAD)
class AppContainer(private val context: Context) {

    // FIX: Changed BASE_URL from HTTPS to HTTP to bypass SSL certificate issues
    private val BASE_URL = "http://private-anon-e766e44b9e-mutualfundapi.apiary-mock.com/" // Changed to HTTP

    // Setup HttpLoggingInterceptor for logging network requests and responses
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY) // Logs request and response bodies
    }

    // NEW HELPER FUNCTION: To read a file from assets
    @RequiresApi(Build.VERSION_CODES.GINGERBREAD)
    private fun getJsonFromAssets(fileName: String): String {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            throw IOException("Error reading asset file: $fileName. ${e.message}", e)
        }
    }

    // UPDATED: Custom Interceptor to read from assets based on the requested URL path
    @RequiresApi(Build.VERSION_CODES.GINGERBREAD)
    private val assetReadingInterceptor = Interceptor { chain ->
        val request = chain.request()
        val url = request.url.toString()
        val pathSegments = request.url.pathSegments

        val assetFileName: String? = when {
            url.endsWith("mutual_fund_holdings.json") -> "holdings.json"
            url.endsWith("holdings.json") -> "holdings.json"
            url.endsWith("transactions.json") -> "transactions.json"
            url.endsWith("portfolio_summary.json") -> "portfolio_summary.json"
            pathSegments.size >= 2 && pathSegments[pathSegments.size - 2] == "fund_details" ->
                "fund_details_${pathSegments.last()}"

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

    @RequiresApi(Build.VERSION_CODES.GINGERBREAD)
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(assetReadingInterceptor)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()) // Using Gson Converter for now
            .build()
    }

    private val mutualFundApiService: MutualFundApiService by lazy {
        retrofit.create(MutualFundApiService::class.java)
    }

    val mutualFundRepository: MutualFundAppRepository by lazy {
        NetworkMutualFundAppRepository(mutualFundApiService)
    }
}