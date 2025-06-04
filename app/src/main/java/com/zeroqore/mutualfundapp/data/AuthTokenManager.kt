// app/src/main/java/com/zeroqore/mutualfundapp/data/AuthTokenManager.kt
package com.zeroqore.mutualfundapp.data

import android.content.Context
import android.content.SharedPreferences

// Define constants for SharedPreferences keys
private const val PREFS_FILE_NAME = "mutual_fund_app_prefs"
private const val KEY_ACCESS_TOKEN = "access_token"
private const val KEY_REFRESH_TOKEN = "refresh_token"
private const val KEY_EXPIRES_IN = "expires_in"
private const val KEY_TOKEN_TYPE = "token_type"
private const val KEY_INVESTOR_ID = "investor_id"
private const val KEY_DISTRIBUTOR_ID = "distributor_id"
private const val KEY_INVESTOR_NAME = "investor_name"

class AuthTokenManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)

    // region Save and Retrieve Tokens/IDs
    fun saveAuthData(
        accessToken: String,
        refreshToken: String,
        expiresIn: Long,
        tokenType: String,
        investorId: String,
        distributorId: String,
        investorName: String?
    ) {
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putLong(KEY_EXPIRES_IN, expiresIn)
            putString(KEY_TOKEN_TYPE, tokenType)
            putString(KEY_INVESTOR_ID, investorId)
            putString(KEY_DISTRIBUTOR_ID, distributorId)
            putString(KEY_INVESTOR_NAME, investorName)
            apply() // Apply changes asynchronously
        }
    }

    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    fun getInvestorId(): String? {
        return prefs.getString(KEY_INVESTOR_ID, null)
    }

    fun getDistributorId(): String? {
        return prefs.getString(KEY_DISTRIBUTOR_ID, null)
    }

    fun getInvestorName(): String? {
        return prefs.getString(KEY_INVESTOR_NAME, null)
    }

    fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }
    // endregion

    // region Clear Data
    fun clearAuthData() {
        prefs.edit().apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_EXPIRES_IN)
            remove(KEY_TOKEN_TYPE)
            remove(KEY_INVESTOR_ID)
            remove(KEY_DISTRIBUTOR_ID)
            remove(KEY_INVESTOR_NAME)
            apply()
        }
    }
    // endregion
}