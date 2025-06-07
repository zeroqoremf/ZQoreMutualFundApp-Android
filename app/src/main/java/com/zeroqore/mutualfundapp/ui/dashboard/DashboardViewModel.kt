// app/src/main/java/com/zeroqore.mutualfundapp/ui/dashboard/DashboardViewModel.kt
package com.zeroqore.mutualfundapp.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zeroqore.mutualfundapp.data.MutualFundHolding
import com.zeroqore.mutualfundapp.data.PortfolioSummary
import com.zeroqore.mutualfundapp.data.MutualFundTransaction
import com.zeroqore.mutualfundapp.data.MutualFundAppRepository
import com.zeroqore.mutualfundapp.util.Results // Corrected import
import com.zeroqore.mutualfundapp.data.AuthTokenManager // Corrected import
import kotlinx.coroutines.launch
import android.util.Log

class DashboardViewModel(
    private val repository: MutualFundAppRepository,
    private val authTokenManager: AuthTokenManager
) : ViewModel() {

    private val _fundHoldings = MutableLiveData<List<MutualFundHolding>>()
    val fundHoldings: LiveData<List<MutualFundHolding>> = _fundHoldings

    // CORRECTED: Make the type nullable if it can ever be null
    private val _portfolioSummary = MutableLiveData<PortfolioSummary?>() // Changed to PortfolioSummary?
    val portfolioSummary: LiveData<PortfolioSummary?> = _portfolioSummary // Changed to PortfolioSummary?

    // CORRECTED: List itself can't be null, but an emptyList() is fine.
    // If the list itself could be null, you'd use List<MutualFundTransaction>?
    // However, in your error case, you're trying to set it to emptyList() which is correct for a non-nullable List.
    // The previous error message refers to _portfolioSummary.postValue(null).
    // Let's re-verify if _recentTransactions.postValue(null) is also happening.
    // If you intend to post null for transactions, it should also be MutableLiveData<List<MutualFundTransaction>?>()
    // But typically, an empty list is preferred over null for collections.
    private val _recentTransactions = MutableLiveData<List<MutualFundTransaction>>() // Keep as is, as emptyList() is used

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        Log.d("DashboardViewModel", "ViewModel initialized. Calling data loading functions.")
        loadFundHoldings()
        loadPortfolioSummary()
        loadRecentTransactions()
    }

    private fun loadFundHoldings() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            _errorMessage.postValue(null)

            val investorId = authTokenManager.getInvestorId()
            val distributorId = authTokenManager.getDistributorId()

            Log.d("DashboardViewModel", "loadFundHoldings: Attempting to fetch holdings for Investor ID: $investorId, Distributor ID: $distributorId")

            if (investorId == null) {
                val errorMsg = "Authentication data (Investor ID) missing for Dashboard. Please log in again."
                _errorMessage.postValue(errorMsg)
                _isLoading.postValue(false)
                _fundHoldings.postValue(emptyList()) // emptyList() is not null, so this is fine
                Log.e("DashboardViewModel", "loadFundHoldings: $errorMsg")
                return@launch
            }

            val result = repository.getHoldings(investorId, distributorId)

            when (result) {
                is Results.Success -> {
                    _fundHoldings.postValue(result.data)
                    Log.d("DashboardViewModel", "loadFundHoldings: Successfully fetched ${result.data.size} fund holdings.")
                }
                is Results.Error -> {
                    val msg = result.message ?: "An unknown error occurred while fetching holdings."
                    _errorMessage.postValue(msg)
                    _fundHoldings.postValue(emptyList())
                    Log.e("DashboardViewModel", "loadFundHoldings: Error loading fund holdings: $msg", result.exception)
                }
                is Results.Loading -> {
                    Log.d("DashboardViewModel", "loadFundHoldings: Repository reported loading state.")
                }
            }
            _isLoading.postValue(false)
        }
    }

    private fun loadPortfolioSummary() {
        viewModelScope.launch {
            val investorId = authTokenManager.getInvestorId()
            val distributorId = authTokenManager.getDistributorId()

            Log.d("DashboardViewModel", "loadPortfolioSummary: Attempting to fetch portfolio summary for Investor ID: $investorId, Distributor ID: $distributorId")

            if (investorId == null) {
                Log.e("DashboardViewModel", "loadPortfolioSummary: Investor ID missing for portfolio summary.")
                _portfolioSummary.postValue(null) // NOW THIS IS ALLOWED
                return@launch
            }

            val result = repository.getPortfolioSummary(investorId, distributorId)
            when (result) {
                is Results.Success -> {
                    _portfolioSummary.postValue(result.data)
                    Log.d("DashboardViewModel", "loadPortfolioSummary: Successfully fetched portfolio summary: ${result.data}")
                }
                is Results.Error -> {
                    Log.e("DashboardViewModel", "loadPortfolioSummary: Error loading portfolio summary: ${result.message}", result.exception)
                    _portfolioSummary.postValue(null) // NOW THIS IS ALLOWED
                }
                is Results.Loading -> {
                    Log.d("DashboardViewModel", "loadPortfolioSummary: Repository reported loading state.")
                }
            }
        }
    }

    private fun loadRecentTransactions() {
        viewModelScope.launch {
            val investorId = authTokenManager.getInvestorId()
            val distributorId = authTokenManager.getDistributorId()

            Log.d("DashboardViewModel", "loadRecentTransactions: Attempting to fetch recent transactions for Investor ID: $investorId, Distributor ID: $distributorId")

            if (investorId == null) {
                Log.e("DashboardViewModel", "loadRecentTransactions: Investor ID missing for recent transactions.")
                _recentTransactions.postValue(emptyList()) // This was already fine
                return@launch
            }

            val result = repository.getTransactions(investorId, distributorId)
            when (result) {
                is Results.Success -> {
                    _recentTransactions.postValue(result.data)
                    Log.d("DashboardViewModel", "loadRecentTransactions: Successfully fetched ${result.data.size} recent transactions.")
                }
                is Results.Error -> {
                    Log.e("DashboardViewModel", "loadRecentTransactions: Error loading recent transactions: ${result.message}", result.exception)
                    _recentTransactions.postValue(emptyList()) // This was already fine
                }
                is Results.Loading -> {
                    Log.d("DashboardViewModel", "loadRecentTransactions: Repository reported loading state.")
                }
            }
        }
    }

    fun refreshAllDashboardData() {
        loadFundHoldings()
        loadPortfolioSummary()
        loadRecentTransactions()
    }

    /**
     * Factory for creating DashboardViewModel with a constructor that takes a repository and AuthTokenManager.
     */
    class Factory(
        private val repository: MutualFundAppRepository,
        private val authTokenManager: AuthTokenManager
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DashboardViewModel(repository, authTokenManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}