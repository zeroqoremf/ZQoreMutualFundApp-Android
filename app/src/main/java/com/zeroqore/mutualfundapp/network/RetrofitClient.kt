// app/src/main/java/com/zeroqore/mutualfundapp/network/RetrofitClient.kt
package com.zeroqore.mutualfundapp.network

import com.zeroqore.mutualfundapp.BuildConfig // Import BuildConfig
// REMOVED: import kotlinx.serialization.json.Json
// REMOVED: import okhttp3.MediaType.Companion.toMediaType

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory // ADDED: Gson Converter Factory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = BuildConfig.BASE_URL

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Log request and response bodies
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor) // Add logging interceptor for debugging
        .connectTimeout(120, TimeUnit.SECONDS) // Connection timeout
        .readTimeout(120, TimeUnit.SECONDS)    // Read timeout
        .writeTimeout(120, TimeUnit.SECONDS)   // Write timeout
        .build()

    // REMOVED: The entire Json block, as it's for Kotlinx Serialization
    /*
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        coerceInputValues = true
    }
    */

    val authService: AuthService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()) // CHANGED: Now uses Gson converter
            .build()
            .create(AuthService::class.java)
    }
}