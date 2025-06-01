// app/src/main/java/com/zeroqore/mutualfundapp/ui/portfolio/PortfolioViewModel.kt
package com.zeroqore.mutualfundapp.ui.portfolio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zeroqore.mutualfundapp.data.AssetAllocation // New import
import com.zeroqore.mutualfundapp.data.MutualFundRepository // Import the interface
import com.zeroqore.mutualfundapp.data.PortfolioSummary // New import

class PortfolioViewModel(private val repository: MutualFundRepository) : ViewModel() {

    private val _portfolioSummary = MutableLiveData<PortfolioSummary>()
    val portfolioSummary: LiveData<PortfolioSummary> = _portfolioSummary

    private val _assetAllocation = MutableLiveData<AssetAllocation>()
    val assetAllocation: LiveData<AssetAllocation> = _assetAllocation

    init {
        // Fetch data when the ViewModel is created
        loadPortfolioData()
    }

    private fun loadPortfolioData() {
        // In a real app, these would typically be done in coroutines for async operations
        _portfolioSummary.value = repository.getPortfolioSummary()
        _assetAllocation.value = repository.getAssetAllocation()
    }

    // Method to refresh data (e.g., for pull-to-refresh)
    fun refreshPortfolioData() {
        loadPortfolioData()
    }
}