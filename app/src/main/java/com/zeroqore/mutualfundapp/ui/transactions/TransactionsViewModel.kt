// app/src/main/java/com/zeroqore/mutualfundapp/ui/transactions/TransactionsViewModel.kt
package com.zeroqore.mutualfundapp.ui.transactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeroqore.mutualfundapp.data.MutualFundAppRepository // Make sure this import is correct (new name)
import com.zeroqore.mutualfundapp.data.MutualFundTransaction // Import the data class
import kotlinx.coroutines.launch
import android.util.Log // Add this import for logging

class TransactionsViewModel(private val repository: MutualFundAppRepository) : ViewModel() { // Ensure correct repository type

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
            try {
                _transactions.value = repository.getTransactions()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load transactions: ${e.message}"
                Log.e("TransactionsViewModel", "Error loading transactions", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}