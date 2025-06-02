// app/src/main/java/com/zeroqore/mutualfundapp/ui/dashboard/DashboardViewModel.kt
package com.zeroqore.mutualfundapp.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeroqore.mutualfundapp.data.MutualFundHolding
import com.zeroqore.mutualfundapp.data.MutualFundAppRepository
import com.zeroqore.mutualfundapp.util.Results // Import the Results sealed class
import kotlinx.coroutines.launch
import android.util.Log // Import Log

class DashboardViewModel(private val repository: MutualFundAppRepository) : ViewModel() {

    private val _fundHoldings = MutableLiveData<List<MutualFundHolding>>()
    val fundHoldings: LiveData<List<MutualFundHolding>> = _fundHoldings

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadFundHoldings()
    }

    private fun loadFundHoldings() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            _errorMessage.postValue(null) // Clear any previous error message

            // Call the repository method, which now returns a Results sealed class
            val result = repository.getHoldings()

            // Handle the different states of the Results sealed class
            when (result) {
                is Results.Success -> {
                    _fundHoldings.postValue(result.data)
                    Log.d("DashboardViewModel", "Fetched ${result.data.size} fund holdings.")
                }
                is Results.Error -> {
                    _errorMessage.postValue(result.message ?: "An unknown error occurred while fetching holdings.")
                    _fundHoldings.postValue(emptyList()) // Set an empty list on error
                    Log.e("DashboardViewModel", "Error loading fund holdings: ${result.message}", result.exception)
                }
                is Results.Loading -> {
                    // This state is typically used for immediate UI feedback (e.g., showing a spinner)
                    // before the actual data/error is returned.
                    // The _isLoading.postValue(true) at the start already handles initial loading state.
                    Log.d("DashboardViewModel", "Repository reported loading state.")
                }
            }
            _isLoading.postValue(false) // Always set to false after completion (success or error)
        }
    }

    fun refreshHoldings() {
        loadFundHoldings()
    }
}