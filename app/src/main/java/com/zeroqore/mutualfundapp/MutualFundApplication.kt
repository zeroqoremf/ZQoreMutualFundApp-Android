// app/src/main/java/com/zeroqore/mutualfundapp/MutualFundApplication.kt
package com.zeroqore.mutualfundapp

import android.app.Application
import com.zeroqore.mutualfundapp.data.AppContainer
import com.zeroqore.mutualfundapp.di.MutualFundViewModelFactory // UPDATED IMPORT

class MutualFundApplication : Application() {

    lateinit var appContainer: AppContainer
    lateinit var viewModelFactory: MutualFundViewModelFactory

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(
            context = applicationContext,
            baseUrl = BuildConfig.BASE_URL,
            useMockAssetInterceptor = BuildConfig.USE_MOCK_ASSET_INTERCEPTOR
        )
        viewModelFactory = MutualFundViewModelFactory(appContainer.mutualFundRepository)
    }
}