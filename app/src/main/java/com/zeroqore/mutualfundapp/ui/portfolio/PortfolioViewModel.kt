// app/src/main/java/com/zeroqore/mutualfundapp/ui/portfolio/PortfolioViewModel.kt
package com.zeroqore.mutualfundapp.ui.portfolio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider // Added this import
import androidx.lifecycle.viewModelScope
import com.zeroqore.mutualfundapp.data.AssetAllocation
import com.zeroqore.mutualfundapp.data.MutualFundAppRepository
import com.zeroqore.mutualfundapp.data.PortfolioSummary
import com.zeroqore.mutualfundapp.util.Results // Import the Results sealed class
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

            val summaryResult = repository.getPortfolioSummary()
            val allocation = repository.getAssetAllocation()

            when (summaryResult) {
                is Results.Success -> {
                    _portfolioSummary.postValue(summaryResult.data)
                    _assetAllocation.postValue(allocation)
                    Log.d("PortfolioViewModel", "Portfolio summary and asset allocation fetched successfully.")
                }
                is Results.Error -> {
                    _errorMessage.postValue(summaryResult.message ?: "An unknown error occurred while fetching portfolio summary.")
                    _portfolioSummary.postValue(PortfolioSummary(0.0, 0.0, 0.0, 0.0, "N/A"))
                    _assetAllocation.postValue(AssetAllocation(0.0, 0.0, 0.0, 0.0, 0.0, 0.0))
                    Log.e("PortfolioViewModel", "Error loading portfolio summary: ${summaryResult.message}", summaryResult.exception)
                }
                is Results.Loading -> {
                    Log.d("PortfolioViewModel", "Portfolio summary is in loading state.")
                }
            }
            _isLoading.postValue(false)
        }
    }

    fun refreshPortfolioData() {
        loadPortfolioData()
    }

    /**
     * Factory for creating PortfolioViewModel with a constructor that takes a repository.
     */
    class Factory(private val repository: MutualFundAppRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PortfolioViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PortfolioViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}