// app/src/main/java/com/zeroqore/mutualfundapp/ui/transactions/TransactionsFragment.kt
package com.zeroqore.mutualfundapp.ui.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // NEW IMPORT
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider // NEW IMPORT
import androidx.recyclerview.widget.LinearLayoutManager // NEW IMPORT
import com.zeroqore.mutualfundapp.MutualFundApplication // NEW IMPORT
import com.zeroqore.mutualfundapp.data.MutualFundAppRepository // Import the interface
import com.zeroqore.mutualfundapp.databinding.FragmentTransactionsBinding // Make sure this import is correct

class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var transactionsAdapter: TransactionsAdapter // Declare the adapter

    // Initialize ViewModel using ViewModelProvider.Factory
    private val transactionsViewModel: TransactionsViewModel by viewModels {
        TransactionsViewModelFactory((activity?.application as MutualFundApplication).container.mutualFundRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView
        binding.transactionsRecyclerView.layoutManager = LinearLayoutManager(context)

        // Observe transactions from the ViewModel
        transactionsViewModel.transactions.observe(viewLifecycleOwner) { transactionsList ->
            // Initialize adapter if not already, or update its data
            if (!::transactionsAdapter.isInitialized) {
                transactionsAdapter = TransactionsAdapter(transactionsList)
                binding.transactionsRecyclerView.adapter = transactionsAdapter
            } else {
                // If you want to update the list, you would need to add a setter in the adapter:
                // transactionsAdapter.updateData(transactionsList)
                // For now, if initialized once, this won't change
                // Or, simply create a new adapter if data is small and changes infrequently
                binding.transactionsRecyclerView.adapter = TransactionsAdapter(transactionsList)
            }

            // If the list is empty, you could show a message
            binding.transactionsRecyclerView.visibility = if (transactionsList.isEmpty()) View.GONE else View.VISIBLE
            // Optionally show a TextView here if list is empty
            // binding.emptyTransactionsMessage.visibility = if (transactionsList.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// NEW: ViewModel Factory for TransactionsViewModel
class TransactionsViewModelFactory(private val repository: MutualFundAppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}