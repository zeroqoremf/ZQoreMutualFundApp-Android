// app/src/main/java/com/zeroqore/mutualfundapp/ui/portfolio/PortfolioViewModel.kt
package com.zeroqore.mutualfundapp.ui.portfolio

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zeroqore.mutualfundapp.data.AssetAllocation // Keep if used for data models, even if not directly displayed
import com.zeroqore.mutualfundapp.data.MutualFundAppRepository
import com.zeroqore.mutualfundapp.data.MutualFundHolding
import com.zeroqore.mutualfundapp.data.PortfolioDisplayItem
import com.zeroqore.mutualfundapp.data.PortfolioSummary
import com.zeroqore.mutualfundapp.util.Results
import com.zeroqore.mutualfundapp.data.AuthTokenManager // IMPORT AUTH TOKEN MANAGER
import kotlinx.coroutines.launch

class PortfolioViewModel(
    private val repository: MutualFundAppRepository,
    private val authTokenManager: AuthTokenManager // ADDED: Inject AuthTokenManager
) : ViewModel() {

    private val _portfolioSummary = MutableLiveData<PortfolioSummary>()
    val portfolioSummary: LiveData<PortfolioSummary> = _portfolioSummary

    private val _portfolioDisplayItems = MutableLiveData<List<PortfolioDisplayItem>>()
    val portfolioDisplayItems: LiveData<List<PortfolioDisplayItem>> = _portfolioDisplayItems

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private var allHoldings: List<MutualFundHolding> = emptyList()
    private val expansionStates: MutableMap<String, Boolean> = mutableMapOf()

    init {
        loadPortfolioData()
    }

    private fun loadPortfolioData() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            _errorMessage.postValue(null)

            // NEW: Retrieve investorId and distributorId from AuthTokenManager
            val investorId = authTokenManager.getInvestorId()
            val distributorId = authTokenManager.getDistributorId()

            if (investorId == null || distributorId == null) {
                val errorMsg = "Authentication data (Investor ID or Distributor ID) missing. Please log in again."
                _errorMessage.postValue(errorMsg)
                _isLoading.postValue(false)
                _portfolioSummary.postValue(PortfolioSummary(0.0, 0.0, 0.0, 0.0, "N/A"))
                allHoldings = emptyList()
                updatePortfolioDisplayItems()
                Log.e("PortfolioViewModel", errorMsg)
                return@launch // Stop execution if IDs are missing
            }

            // Fetch both summary and holdings concurrently, passing the IDs
            val summaryResult = repository.getPortfolioSummary(investorId, distributorId)
            val holdingsResult = repository.getHoldings(investorId, distributorId)

            when (summaryResult) {
                is Results.Success -> {
                    _portfolioSummary.postValue(summaryResult.data)
                    Log.d("PortfolioViewModel", "Portfolio summary fetched successfully.")
                }
                is Results.Error -> {
                    _errorMessage.postValue(summaryResult.message ?: "An unknown error occurred while fetching portfolio summary.")
                    _portfolioSummary.postValue(PortfolioSummary(0.0, 0.0, 0.0, 0.0, "N/A"))
                    Log.e("PortfolioViewModel", "Error loading portfolio summary: ${summaryResult.message}", summaryResult.exception)
                }
                is Results.Loading -> {
                    Log.d("PortfolioViewModel", "Portfolio summary is in loading state.")
                }
            }

            when (holdingsResult) {
                is Results.Success -> {
                    allHoldings = holdingsResult.data // Store raw holdings
                    allHoldings.map { it.fundType }.distinct().forEach { fundType ->
                        expansionStates.putIfAbsent(fundType, false)
                    }
                    updatePortfolioDisplayItems() // Process holdings for display
                    Log.d("PortfolioViewModel", "Fund holdings fetched successfully. Count: ${allHoldings.size}")
                }
                is Results.Error -> {
                    _errorMessage.postValue(holdingsResult.message ?: "An unknown error occurred while fetching fund holdings.")
                    allHoldings = emptyList() // Clear holdings on error
                    updatePortfolioDisplayItems() // Update display items with empty list
                    Log.e("PortfolioViewModel", "Error loading fund holdings: ${holdingsResult.message}", holdingsResult.exception)
                }
                is Results.Loading -> {
                    Log.d("PortfolioViewModel", "Fund holdings are in loading state.")
                }
            }

            _isLoading.postValue(false)
        }
    }

    private fun updatePortfolioDisplayItems() {
        val displayList = mutableListOf<PortfolioDisplayItem>()
        val groupedHoldings = allHoldings.groupBy { it.fundType }

        val sortedFundTypes = groupedHoldings.keys.sortedWith(compareByDescending {
            when (it) {
                "Equity" -> 3
                "Hybrid" -> 2
                "Debt" -> 1
                else -> 0
            }
        }).reversed()

        for (fundType in sortedFundTypes) {
            val fundsInType = groupedHoldings[fundType] ?: emptyList()

            var typeTotalCurrentValue = 0.0
            var typeTotalInvestedValue = 0.0

            for (holding in fundsInType) {
                typeTotalCurrentValue += holding.currentValue
                typeTotalInvestedValue += holding.purchasePrice
            }

            val typeGainLossAbsolute = typeTotalCurrentValue - typeTotalInvestedValue
            val typeGainLossPercentage = if (typeTotalInvestedValue != 0.0) {
                (typeGainLossAbsolute / typeTotalInvestedValue) * 100
            } else {
                0.0
            }

            val isExpanded = expansionStates[fundType] ?: false

            val header = PortfolioDisplayItem.AssetTypeHeader(
                id = "${fundType}_Header",
                fundType = fundType,
                totalCurrentValue = typeTotalCurrentValue,
                totalInvestedValue = typeTotalInvestedValue,
                gainLossAbsolute = typeGainLossAbsolute,
                gainLossPercentage = typeGainLossPercentage,
                isExpanded = isExpanded
            )
            displayList.add(header)

            if (isExpanded) {
                fundsInType.forEach { holding ->
                    val absoluteReturn = holding.currentValue - holding.purchasePrice
                    val percentageReturn = if (holding.purchasePrice != 0.0) {
                        (absoluteReturn / holding.purchasePrice) * 100
                    } else {
                        0.0
                    }
                    displayList.add(PortfolioDisplayItem.FundHoldingItem(
                        holding = holding,
                        absoluteReturn = absoluteReturn,
                        percentageReturn = percentageReturn
                    ))
                }
            }
        }
        _portfolioDisplayItems.postValue(displayList)
    }

    fun toggleAssetTypeExpansion(fundType: String) {
        val currentExpandedState = expansionStates[fundType] ?: false
        expansionStates[fundType] = !currentExpandedState
        updatePortfolioDisplayItems()
    }

    fun toggleAllAssetTypes(expand: Boolean) {
        expansionStates.keys.forEach { fundType ->
            expansionStates[fundType] = expand
        }
        updatePortfolioDisplayItems()
    }

    fun refreshPortfolioData() {
        loadPortfolioData()
    }

    /**
     * Factory for creating PortfolioViewModel with a constructor that takes a repository and AuthTokenManager.
     */
    class Factory(
        private val repository: MutualFundAppRepository,
        private val authTokenManager: AuthTokenManager // ADDED: AuthTokenManager to Factory
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PortfolioViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PortfolioViewModel(repository, authTokenManager) as T // Pass authTokenManager
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}