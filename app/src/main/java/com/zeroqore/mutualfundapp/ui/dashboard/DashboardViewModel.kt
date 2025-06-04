// app/src/main/java/com/zeroqore.mutualfundapp/ui/dashboard/DashboardViewModel.kt
package com.zeroqore.mutualfundapp.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zeroqore.mutualfundapp.data.MutualFundHolding
import com.zeroqore.mutualfundapp.data.MutualFundAppRepository
import com.zeroqore.mutualfundapp.util.Results
import com.zeroqore.mutualfundapp.data.AuthTokenManager // ADDED: Import AuthTokenManager
import kotlinx.coroutines.launch
import android.util.Log

class DashboardViewModel(
    private val repository: MutualFundAppRepository,
    private val authTokenManager: AuthTokenManager // ADDED: Inject AuthTokenManager
) : ViewModel() {

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
            _errorMessage.postValue(null)

            // NEW: Retrieve investorId and distributorId from AuthTokenManager
            val investorId = authTokenManager.getInvestorId()
            val distributorId = authTokenManager.getDistributorId()

            if (investorId == null || distributorId == null) {
                val errorMsg = "Authentication data (Investor ID or Distributor ID) missing for Dashboard. Please log in again."
                _errorMessage.postValue(errorMsg)
                _isLoading.postValue(false)
                _fundHoldings.postValue(emptyList())
                Log.e("DashboardViewModel", errorMsg)
                return@launch // Stop execution if IDs are missing
            }

            // MODIFIED: Pass investorId and distributorId to repository.getHoldings()
            val result = repository.getHoldings(investorId, distributorId)

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
     * Factory for creating DashboardViewModel with a constructor that takes a repository and AuthTokenManager.
     */
    class Factory(
        private val repository: MutualFundAppRepository,
        private val authTokenManager: AuthTokenManager // ADDED: AuthTokenManager to Factory constructor
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                // MODIFIED: Pass both repository and authTokenManager to DashboardViewModel
                return DashboardViewModel(repository, authTokenManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}