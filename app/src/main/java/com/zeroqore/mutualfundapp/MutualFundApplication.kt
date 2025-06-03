package com.zeroqore.mutualfundapp

import android.app.Application
import com.zeroqore.mutualfundapp.data.AppContainer
//import com.zeroqore.mutualfundapp.data.AppContainerImpl // Assuming you'll rename AppContainer to AppContainerImpl

class MutualFundApplication : Application() {

    // Use a lateinit var to initialize it once
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        // Initialize AppContainer
        container = AppContainer(
            this,
            BuildConfig.BASE_URL,
            BuildConfig.USE_MOCK_ASSET_INTERCEPTOR
        )
        // If you decide to rename AppContainer to AppContainerImpl,
        // change the line above to:
        // container = AppContainerImpl(this, BuildConfig.BASE_URL, BuildConfig.USE_MOCK_ASSET_INTERCEPTOR)
    }
}