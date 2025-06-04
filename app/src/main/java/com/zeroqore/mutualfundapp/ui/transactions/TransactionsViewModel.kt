package com.zeroqore.mutualfundapp.ui.transactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeroqore.mutualfundapp.data.MutualFundAppRepository
import com.zeroqore.mutualfundapp.data.MutualFundTransaction
import com.zeroqore.mutualfundapp.util.Results
import com.zeroqore.mutualfundapp.data.AuthTokenManager // ADDED: Import AuthTokenManager
import kotlinx.coroutines.launch
import android.util.Log

class TransactionsViewModel(
    private val repository: MutualFundAppRepository,
    private val authTokenManager: AuthTokenManager // ADDED: Inject AuthTokenManager
) : ViewModel() {

    private val _transactions = MutableLiveData<List<MutualFundTransaction>>()
    val transactions: LiveData<List<MutualFundTransaction>> = _transactions

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadTransactions()
    }

    fun loadTransactions() {
        _isLoading.value = true
        _errorMessage.value = null // Clear any previous errors

        viewModelScope.launch {
            // NEW: Retrieve investorId and distributorId from AuthTokenManager
            val investorId = authTokenManager.getInvestorId()
            val distributorId = authTokenManager.getDistributorId()

            if (investorId == null || distributorId == null) {
                val errorMsg = "Authentication data (Investor ID or Distributor ID) missing for Transactions. Please log in again."
                _errorMessage.value = errorMsg
                _isLoading.value = false
                _transactions.value = emptyList()
                Log.e("TransactionsViewModel", errorMsg)
                return@launch // Stop execution if IDs are missing
            }

            // MODIFIED: Pass investorId and distributorId to repository.getTransactions()
            val result = repository.getTransactions(investorId, distributorId)

            when (result) {
                is Results.Success -> {
                    _transactions.value = result.data
                    Log.d("TransactionsViewModel", "Fetched ${result.data.size} transactions.")
                }
                is Results.Error -> {
                    _errorMessage.value = result.message ?: "An unknown error occurred while fetching transactions."
                    _transactions.value = emptyList() // Set an empty list on error
                    Log.e("TransactionsViewModel", "Error loading transactions: ${result.message}", result.exception)
                }
                is Results.Loading -> {
                    Log.d("TransactionsViewModel", "Repository reported loading state for transactions.")
                }
            }
            _isLoading.value = false // Always set to false after completion (success or error)
        }
    }
}