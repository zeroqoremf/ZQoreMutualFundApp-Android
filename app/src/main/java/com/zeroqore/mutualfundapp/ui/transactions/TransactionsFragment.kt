// app/src/main/java/com/zeroqore/mutualfundapp/ui/transactions/TransactionsFragment.kt
package com.zeroqore.mutualfundapp.ui.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.zeroqore.mutualfundapp.MutualFundApplication
import com.zeroqore.mutualfundapp.data.MutualFundAppRepository
import com.zeroqore.mutualfundapp.databinding.FragmentTransactionsBinding
import com.zeroqore.mutualfundapp.data.AuthTokenManager // ADDED: Import AuthTokenManager

class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var transactionsAdapter: TransactionsAdapter

    // Initialize ViewModel using ViewModelProvider.Factory
    private val transactionsViewModel: TransactionsViewModel by viewModels {
        // MODIFIED: Pass both mutualFundRepository AND authTokenManager to the Factory
        TransactionsViewModelFactory(
            (activity?.application as MutualFundApplication).container.mutualFundRepository,
            (activity?.application as MutualFundApplication).container.authTokenManager // ADDED: Pass AuthTokenManager
        )
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
            if (!::transactionsAdapter.isInitialized) {
                transactionsAdapter = TransactionsAdapter(transactionsList)
                binding.transactionsRecyclerView.adapter = transactionsAdapter
            } else {
                binding.transactionsRecyclerView.adapter = TransactionsAdapter(transactionsList)
            }

            binding.transactionsRecyclerView.visibility = if (transactionsList.isEmpty()) View.GONE else View.VISIBLE
        }

        // ADDED: Observe loading and error messages
        transactionsViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarTransactions.visibility = if (isLoading) View.VISIBLE else View.GONE
            // Optionally hide RecyclerView when loading if no data
            if (isLoading && transactionsViewModel.transactions.value.isNullOrEmpty()) {
                binding.transactionsRecyclerView.visibility = View.GONE
                binding.emptyTransactionsMessage.visibility = View.GONE
            }
        }

        transactionsViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrBlank()) {
                binding.emptyTransactionsMessage.text = errorMessage // Use empty message for error display
                binding.emptyTransactionsMessage.visibility = View.VISIBLE
                binding.transactionsRecyclerView.visibility = View.GONE
            } else {
                binding.emptyTransactionsMessage.visibility = View.GONE
            }
        }

        // Initial load or refresh if needed (ViewModel already loads in init)
        // You can add swipe-to-refresh or a FAB for refresh if desired
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// NEW: ViewModel Factory for TransactionsViewModel
// MODIFIED: Now accepts AuthTokenManager
class TransactionsViewModelFactory(
    private val repository: MutualFundAppRepository,
    private val authTokenManager: AuthTokenManager // ADDED: AuthTokenManager to Factory constructor
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // MODIFIED: Pass both repository and authTokenManager to TransactionsViewModel
            return TransactionsViewModel(repository, authTokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}