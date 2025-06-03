// app/src/main/java/com/zeroqore.mutualfundapp/ui/dashboard/DashboardViewModel.kt
package com.zeroqore.mutualfundapp.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider // Import ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zeroqore.mutualfundapp.data.MutualFundHolding
import com.zeroqore.mutualfundapp.data.MutualFundAppRepository
import com.zeroqore.mutualfundapp.util.Results
import kotlinx.coroutines.launch
import android.util.Log

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

            val result = repository.getHoldings()

            when (result) {
                is Results.Success -> {
                    _fundHoldings.postValue(result.data)
                    Log.d("DashboardViewModel", "Fetched ${result.data.size} fund holdings.")
                }
                is Results.Error -> {
                    _errorMessage.postValue(result.message ?: "An unknown error occurred while fetching holdings.")
                    _fundHoldings.postValue(emptyList())
                    Log.e("DashboardViewModel", "Error loading fund holdings: ${result.message}", result.exception)
                }
                is Results.Loading -> {
                    Log.d("DashboardViewModel", "Repository reported loading state.")
                }
            }
            _isLoading.postValue(false)
        }
    }

    fun refreshHoldings() {
        loadFundHoldings()
    }

    /**
     * Factory for creating DashboardViewModel with a constructor that takes a repository.
     */
    class Factory(private val repository: MutualFundAppRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DashboardViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}