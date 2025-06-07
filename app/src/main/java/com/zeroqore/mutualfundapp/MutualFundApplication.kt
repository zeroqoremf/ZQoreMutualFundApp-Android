package com.zeroqore.mutualfundapp

import android.app.Application
import com.zeroqore.mutualfundapp.data.AppContainer

class MutualFundApplication : Application() {

    // Use a lateinit var to initialize it once
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        // Initialize AppContainer with the new BuildConfig flags
        container = AppContainer(
            this,
            BuildConfig.BASE_URL,
            BuildConfig.USE_LIVE_LOGIN_API,     // NEW: Pass the flag for live login API
            BuildConfig.USE_DASHBOARD_MOCKS     // NEW: Pass the flag for dashboard data mocks
        )
    }
}