// app/src/main/java/com/zeroqore/mutualfundapp/MutualFundApplication.kt
package com.zeroqore.mutualfundapp

import android.app.Application
import com.zeroqore.mutualfundapp.data.AppContainer // Make sure this import exists

class MutualFundApplication : Application() {

    // Declare appContainer as lateinit var
    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        // Initialize AppContainer, passing 'this' (which is the Application context)
        appContainer = AppContainer(this) // CHANGED: Pass 'this' as context
    }
}