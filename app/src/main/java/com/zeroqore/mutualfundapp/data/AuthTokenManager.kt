package com.zeroqore.mutualfundapp.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.io.IOException
import java.security.GeneralSecurityException

// Define constants for SharedPreferences keys
private const val PREFS_FILE_NAME = "mutual_fund_app_prefs"
private const val KEY_ACCESS_TOKEN = "access_token"
private const val KEY_REFRESH_TOKEN = "refresh_token"
// RENAMED: to clarify it stores the absolute timestamp when the token expires
private const val KEY_EXPIRES_IN_TIMESTAMP = "expires_in_timestamp"
private const val KEY_TOKEN_TYPE = "token_type"
private const val KEY_INVESTOR_ID = "investor_id"
private const val KEY_DISTRIBUTOR_ID = "distributor_id"
private const val KEY_INVESTOR_NAME = "investor_name"

class AuthTokenManager(context: Context) {

    private val prefs: SharedPreferences

    init {
        prefs = try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

            EncryptedSharedPreferences.create(
                PREFS_FILE_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: GeneralSecurityException) {
            Log.e("AuthTokenManager", "GeneralSecurityException creating EncryptedSharedPreferences: ${e.message}", e)
            // Fallback to regular SharedPreferences if encryption fails
            context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
        } catch (e: IOException) {
            Log.e("AuthTokenManager", "IOException creating EncryptedSharedPreferences: ${e.message}", e)
            // Fallback to regular SharedPreferences if encryption fails
            context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
        }
        Log.d("AuthTokenManager", "SharedPreferences initialized. Type: ${prefs::class.simpleName}")
    }

    // region Save and Retrieve Tokens/IDs
    fun saveAuthData(
        accessToken: String,
        refreshToken: String,
        // expiresIn is the duration in seconds, as received from the backend
        expiresInSeconds: Long, // Parameter name changed for clarity
        tokenType: String,
        investorId: String,
        distributorId: String?, // Already correctly nullable
        investorName: String? // Already correctly nullable
    ) {
        // Calculate the absolute expiry timestamp
        val expiryTimestampMillis = System.currentTimeMillis() + expiresInSeconds * 1000

        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putLong(KEY_EXPIRES_IN_TIMESTAMP, expiryTimestampMillis) // Store the calculated timestamp
            putString(KEY_TOKEN_TYPE, tokenType)
            putString(KEY_INVESTOR_ID, investorId)
            putString(KEY_DISTRIBUTOR_ID, distributorId) // This correctly handles null strings
            putString(KEY_INVESTOR_NAME, investorName)
            apply()
        }
        Log.d("AuthTokenManager", "Saved: Investor ID=$investorId, Distributor ID=$distributorId, Access Token=${accessToken.take(10)}...")
    }

    fun getAccessToken(): String? {
        val token = prefs.getString(KEY_ACCESS_TOKEN, null)
        Log.d("AuthTokenManager", "Retrieved Access Token: ${token?.take(10)}...")
        return token
    }

    fun getRefreshToken(): String? {
        val token = prefs.getString(KEY_REFRESH_TOKEN, null)
        Log.d("AuthTokenManager", "Retrieved Refresh Token: ${token?.take(10)}...")
        return token
    }

    fun getInvestorId(): String? {
        val id = prefs.getString(KEY_INVESTOR_ID, null)
        Log.d("AuthTokenManager", "Retrieved Investor ID: $id")
        return id
    }

    fun getDistributorId(): String? {
        val id = prefs.getString(KEY_DISTRIBUTOR_ID, null)
        Log.d("AuthTokenManager", "Retrieved Distributor ID: $id")
        return id
    }

    fun getInvestorName(): String? {
        val name = prefs.getString(KEY_INVESTOR_NAME, null)
        Log.d("AuthTokenManager", "Retrieved Investor Name: $name")
        return name
    }

    fun isLoggedIn(): Boolean {
        // Check if access token exists AND if it has not expired
        val accessToken = getAccessToken() // This will now log its retrieval
        val expiryTimestamp = prefs.getLong(KEY_EXPIRES_IN_TIMESTAMP, 0L)
        val currentTime = System.currentTimeMillis()
        val loggedIn = accessToken != null && expiryTimestamp > currentTime
        Log.d("AuthTokenManager", "isLoggedIn check: Token present=${accessToken != null}, Expired=${expiryTimestamp <= currentTime}, Result=$loggedIn")
        return loggedIn
    }
    // endregion

    // region Clear Data
    fun clearAuthData() {
        prefs.edit().apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_EXPIRES_IN_TIMESTAMP) // Use the new key for removal
            remove(KEY_TOKEN_TYPE)
            remove(KEY_INVESTOR_ID)
            remove(KEY_DISTRIBUTOR_ID)
            remove(KEY_INVESTOR_NAME)
            apply()
        }
        Log.d("AuthTokenManager", "Auth data cleared.")
    }
    // endregion
}