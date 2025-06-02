package com.zeroqore.mutualfundapp.ui.transactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeroqore.mutualfundapp.data.MutualFundAppRepository
import com.zeroqore.mutualfundapp.data.MutualFundTransaction
import com.zeroqore.mutualfundapp.util.Results // Import the Results sealed class
import kotlinx.coroutines.launch
import android.util.Log

class TransactionsViewModel(private val repository: MutualFundAppRepository) : ViewModel() {

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
            // Call the repository method, which now returns a Results sealed class
            val result = repository.getTransactions()

            // Handle the different states of the Results sealed class
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
                    // This state is typically used for immediate UI feedback (e.g., showing a spinner)
                    // before the actual data/error is returned.
                    // The _isLoading.value = true at the start already handles initial loading state.
                    Log.d("TransactionsViewModel", "Repository reported loading state for transactions.")
                }
            }
            _isLoading.value = false // Always set to false after completion (success or error)
        }
    }
}