// app/src/main/java/com/zeroqore/mutualfundapp/ui/portfolio/PortfolioViewModel.kt
package com.zeroqore.mutualfundapp.ui.portfolio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeroqore.mutualfundapp.data.AssetAllocation
import com.zeroqore.mutualfundapp.data.MutualFundAppRepository
import com.zeroqore.mutualfundapp.data.PortfolioSummary
import kotlinx.coroutines.launch
import android.util.Log

class PortfolioViewModel(private val repository: MutualFundAppRepository) : ViewModel() {

    private val _portfolioSummary = MutableLiveData<PortfolioSummary>()
    val portfolioSummary: LiveData<PortfolioSummary> = _portfolioSummary

    private val _assetAllocation = MutableLiveData<AssetAllocation>()
    val assetAllocation: LiveData<AssetAllocation> = _assetAllocation

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadPortfolioData()
    }

    private fun loadPortfolioData() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            _errorMessage.postValue(null)

            try {
                val summary = repository.getPortfolioSummary()
                val allocation = repository.getAssetAllocation()

                _portfolioSummary.postValue(summary)
                _assetAllocation.postValue(allocation)

                Log.d("PortfolioViewModel", "Portfolio summary and asset allocation fetched successfully.")
            } catch (e: Exception) {
                _errorMessage.postValue("Failed to load portfolio data: ${e.message}")
                // FIX: Added 'lastUpdated' parameter with a default value "N/A"
                _portfolioSummary.postValue(PortfolioSummary(0.0, 0.0, 0.0, 0.0, "N/A"))
                _assetAllocation.postValue(AssetAllocation(0.0, 0.0, 0.0, 0.0, 0.0, 0.0))
                Log.e("PortfolioViewModel", "Error loading portfolio data", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun refreshPortfolioData() {
        loadPortfolioData()
    }
}