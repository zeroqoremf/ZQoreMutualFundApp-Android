// app/src/main/java/com/zeroqore/mutualfundapp/ui/dashboard/DashboardViewModel.kt
package com.zeroqore.mutualfundapp.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zeroqore.mutualfundapp.data.MutualFundHolding
import com.zeroqore.mutualfundapp.data.MutualFundRepository // IMPORTANT: Ensure this import is correct

class DashboardViewModel(private val repository: MutualFundRepository) : ViewModel() {

    private val _fundHoldings = MutableLiveData<List<MutualFundHolding>>()
    val fundHoldings: LiveData<List<MutualFundHolding>> = _fundHoldings

    init {
        // Fetch data when the ViewModel is created
        loadFundHoldings()
    }

    private fun loadFundHoldings() {
        // In a real app, this would typically be done in a coroutine for async operations
        _fundHoldings.value = repository.getFundHoldings()
    }

    // Method to refresh data (e.g., for pull-to-refresh)
    fun refreshHoldings() {
        loadFundHoldings()
    }
}