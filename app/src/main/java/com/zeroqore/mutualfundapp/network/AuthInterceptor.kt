// app/src/main/java/com/zeroqore/mutualfundapp/network/AuthInterceptor.kt
package com.zeroqore.mutualfundapp.network

import com.zeroqore.mutualfundapp.data.AuthTokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val authTokenManager: AuthTokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Get the access token from SharedPreferences
        val accessToken = authTokenManager.getAccessToken()

        // If an access token exists, add it to the request header
        val requestBuilder = originalRequest.newBuilder()
        if (accessToken != null && accessToken.isNotEmpty()) {
            requestBuilder.header("Authorization", "Bearer $accessToken")
        }

        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}