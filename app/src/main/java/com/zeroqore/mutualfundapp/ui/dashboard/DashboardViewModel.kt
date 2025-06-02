// app/src/main/java/com/zeroqore/mutualfundapp/ui/dashboard/DashboardViewModel.kt
package com.zeroqore.mutualfundapp.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeroqore.mutualfundapp.data.MutualFundHolding
import com.zeroqore.mutualfundapp.data.MutualFundAppRepository
import kotlinx.coroutines.launch
import android.util.Log // Import Log

class DashboardViewModel(private val repository: MutualFundAppRepository) : ViewModel() {

    private val _fundHoldings = MutableLiveData<List<MutualFundHolding>>()
    val fundHoldings: LiveData<List<MutualFundHolding>> = _fundHoldings

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>() // MutableLiveData allows null
    val errorMessage: LiveData<String?> = _errorMessage // NOW PUBLIC LIVE DATA ALSO ALLOWS NULL

    init {
        loadFundHoldings() // This internal function name is fine, but it will now call the new API method
    }

    private fun loadFundHoldings() { // Renamed for clarity to loadHoldings, but keeping for now as is
        viewModelScope.launch {
            _isLoading.postValue(true)
            _errorMessage.postValue(null) // Clear any previous error message

            try {
                // *** IMPORTANT CHANGE HERE ***
                val holdings = repository.getHoldings() // Changed from getFundHoldings() to getHoldings()
                _fundHoldings.postValue(holdings)
                Log.d("DashboardViewModel", "Fetched ${holdings.size} fund holdings.")
            } catch (e: Exception) {
                _errorMessage.postValue("Failed to load funds: ${e.message}")
                _fundHoldings.postValue(emptyList()) // Set an empty list on error
                Log.e("DashboardViewModel", "Error loading fund holdings", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun refreshHoldings() {
        loadFundHoldings()
    }
}