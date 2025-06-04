// app/src/main/java/com/zeroqore/mutualfundapp/ui/portfolio/PortfolioViewModel.kt
package com.zeroqore.mutualfundapp.ui.portfolio

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zeroqore.mutualfundapp.data.AssetAllocation
import com.zeroqore.mutualfundapp.data.MutualFundAppRepository
import com.zeroqore.mutualfundapp.data.MutualFundHolding // Import MutualFundHolding
import com.zeroqore.mutualfundapp.data.PortfolioDisplayItem // Import the new sealed class
import com.zeroqore.mutualfundapp.data.PortfolioSummary
import com.zeroqore.mutualfundapp.util.Results
import kotlinx.coroutines.launch

class PortfolioViewModel(private val repository: MutualFundAppRepository) : ViewModel() {

    private val _portfolioSummary = MutableLiveData<PortfolioSummary>()
    val portfolioSummary: LiveData<PortfolioSummary> = _portfolioSummary

    // REMOVE: This LiveData will no longer be directly observed by the Fragment for asset allocation
    // private val _assetAllocation = MutableLiveData<AssetAllocation>()
    // val assetAllocation: LiveData<AssetAllocation> = _assetAllocation

    // ADDED: New LiveData for the expandable list of portfolio items
    private val _portfolioDisplayItems = MutableLiveData<List<PortfolioDisplayItem>>()
    val portfolioDisplayItems: LiveData<List<PortfolioDisplayItem>> = _portfolioDisplayItems

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Store the raw holdings to re-process when expand/collapse state changes
    private var allHoldings: List<MutualFundHolding> = emptyList()
    // Store the expansion state for each asset type
    private val expansionStates: MutableMap<String, Boolean> = mutableMapOf()

    init {
        loadPortfolioData()
    }

    private fun loadPortfolioData() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            _errorMessage.postValue(null)

            // Fetch both summary and holdings concurrently
            val summaryDeferred = repository.getPortfolioSummary()
            val holdingsDeferred = repository.getHoldings() // Now explicitly fetching holdings

            val summaryResult = summaryDeferred
            val holdingsResult = holdingsDeferred

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
                    // This state is typically handled by _isLoading.postValue(true)
                    Log.d("PortfolioViewModel", "Portfolio summary is in loading state.")
                }
            }

            when (holdingsResult) {
                is Results.Success -> {
                    allHoldings = holdingsResult.data // Store raw holdings
                    // Ensure expansion states are initialized for new fund types
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

    // New function to process raw holdings into display items
    private fun updatePortfolioDisplayItems() {
        val displayList = mutableListOf<PortfolioDisplayItem>()
        val groupedHoldings = allHoldings.groupBy { it.fundType } // Group by fundType

        // Sort fund types for consistent display (e.g., Equity, Debt, Hybrid)
        val sortedFundTypes = groupedHoldings.keys.sortedWith(compareByDescending {
            when (it) {
                "Equity" -> 3
                "Hybrid" -> 2
                "Debt" -> 1
                else -> 0 // Other types would come first if not specified
            }
        }).reversed() // Reverse to get Equity first, then Hybrid, then Debt, then Others

        for (fundType in sortedFundTypes) {
            val fundsInType = groupedHoldings[fundType] ?: emptyList()

            // Calculate summary for the header
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
                // Add individual fund items if expanded
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

    // Function called from Fragment when a header is clicked
    fun toggleAssetTypeExpansion(fundType: String) {
        val currentExpandedState = expansionStates[fundType] ?: false
        expansionStates[fundType] = !currentExpandedState
        updatePortfolioDisplayItems() // Regenerate the list to reflect the new state
    }

    // Function to expand/collapse all asset types (from screenshot)
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