// app/src/main/java/com/zeroqore/mutualfundapp/di/MutualFundViewModelFactory.kt
package com.zeroqore.mutualfundapp.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zeroqore.mutualfundapp.data.MutualFundAppRepository
import com.zeroqore.mutualfundapp.data.AuthTokenManager
import com.zeroqore.mutualfundapp.ui.dashboard.DashboardViewModel
import com.zeroqore.mutualfundapp.ui.portfolio.PortfolioViewModel
import java.lang.IllegalArgumentException

// Custom ViewModelFactory to provide dependencies to ViewModels
class MutualFundViewModelFactory(
    private val repository: MutualFundAppRepository,
    private val authTokenManager: AuthTokenManager // AuthTokenManager is already here, which is correct
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // MODIFIED: Now passing authTokenManager to DashboardViewModel as well
            return DashboardViewModel(repository, authTokenManager) as T
        } else if (modelClass.isAssignableFrom(PortfolioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // This line was already correct from the previous step
            return PortfolioViewModel(repository, authTokenManager) as T
        }
        // Add more ViewModel cases here as your app grows
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}