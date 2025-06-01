// app/src/main/java/com/zeroqore/mutualfundapp/ui/transactions/TransactionsViewModel.kt
package com.zeroqore.mutualfundapp.ui.transactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zeroqore.mutualfundapp.data.MutualFundRepository // Import the repository interface
import com.zeroqore.mutualfundapp.data.MutualFundTransaction // Import the new data class

class TransactionsViewModel(private val repository: MutualFundRepository) : ViewModel() {

    private val _transactions = MutableLiveData<List<MutualFundTransaction>>()
    val transactions: LiveData<List<MutualFundTransaction>> = _transactions

    init {
        // Fetch transactions when the ViewModel is created
        loadTransactions()
    }

    private fun loadTransactions() {
        // In a real app, this would typically be an asynchronous operation (e.g., using coroutines)
        _transactions.value = repository.getTransactions()
    }

    // Optional: Method to refresh transactions (e.g., for pull-to-refresh)
    fun refreshTransactions() {
        loadTransactions()
    }
}