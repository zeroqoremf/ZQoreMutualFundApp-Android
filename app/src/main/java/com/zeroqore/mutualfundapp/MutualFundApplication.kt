// app/src/main/java/com/zeroqore/mutualfundapp/MutualFundApplication.kt
package com.zeroqore.mutualfundapp

import android.app.Application
import com.zeroqore.mutualfundapp.data.AppContainer // IMPORTANT: Ensure this import is correct

class MutualFundApplication : Application() {
    // AppContainer instance, created once for the entire application lifecycle
    // 'lateinit' means it will be initialized later, but before first use.
    // It's initialized in onCreate().
    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer()
    }
}