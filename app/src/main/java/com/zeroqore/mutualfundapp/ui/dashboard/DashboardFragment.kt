// app/src/main/java/com/zeroqore/mutualfundapp/ui/dashboard/DashboardFragment.kt
package com.zeroqore.mutualfundapp.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // NEW IMPORT for viewModels delegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider // NEW IMPORT for ViewModelProvider.Factory
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.zeroqore.mutualfundapp.MutualFundApplication // NEW IMPORT for your Application class
import com.zeroqore.mutualfundapp.data.MutualFundHolding
import com.zeroqore.mutualfundapp.data.MutualFundRepository // NEW IMPORT for the Repository interface
import com.zeroqore.mutualfundapp.databinding.FragmentDashboardBinding
import java.text.NumberFormat
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    // Initialize ViewModel using ViewModelProvider.Factory
    // This connects the Fragment to the ViewModel, providing the Repository via AppContainer
    private val dashboardViewModel: DashboardViewModel by viewModels {
        DashboardViewModelFactory((activity?.application as MutualFundApplication).appContainer.mutualFundRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // OBSERVE fund holdings from the ViewModel
        // The UI will update whenever the data in fundHoldings LiveData changes
        dashboardViewModel.fundHoldings.observe(viewLifecycleOwner) { holdings ->
            // Setup RecyclerView only when data is available
            setupRecyclerView(holdings)
            // Update Portfolio Summary with the observed data
            updatePortfolioSummary(holdings)
        }
        // Removed the direct call to generateDummyHoldings()
    }

    private fun setupRecyclerView(holdings: List<MutualFundHolding>) {
        val adapter = MutualFundHoldingsAdapter(holdings) { clickedHolding ->
            val action = DashboardFragmentDirections.actionDashboardFragmentToFundDetailFragment(clickedHolding)
            findNavController().navigate(action)
        }
        binding.fundHoldingsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.fundHoldingsRecyclerView.adapter = adapter
    }

    private fun updatePortfolioSummary(holdings: List<MutualFundHolding>) {
        var totalInvested = 0.0
        var totalCurrentValue = 0.0

        for (holding in holdings) {
            // Ensure correct property name (purchasePrice) is used as per your MutualFundHolding data class
            totalInvested += (holding.purchasePrice * holding.units)
            totalCurrentValue += holding.currentValue
        }

        val overallGainLoss = totalCurrentValue - totalInvested
        val overallPercentageChange = if (totalInvested != 0.0) {
            (overallGainLoss / totalInvested) * 100
        } else {
            0.0
        }

        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())

        binding.totalInvestedValueTextView.text = currencyFormatter.format(totalInvested)
        binding.totalCurrentValueTextView.text = currencyFormatter.format(totalCurrentValue)

        binding.todayAbsGainLossTextView.text = String.format(
            Locale.getDefault(),
            "%s%s",
            if (overallGainLoss >= 0) "+" else "",
            currencyFormatter.format(overallGainLoss)
        )
        binding.todayAbsGainLossTextView.setTextColor(if (overallGainLoss >= 0) Color.parseColor("#4CAF50") else Color.parseColor("#F44336"))

        binding.todayPctChangeTextView.text = String.format(Locale.getDefault(), "%.2f%%", overallPercentageChange)
        binding.todayPctChangeTextView.setTextColor(if (overallGainLoss >= 0) Color.parseColor("#4CAF50") else Color.parseColor("#F44336"))


        binding.totalOverallGainLossTextView.text = String.format(
            Locale.getDefault(),
            "%s%s",
            if (overallGainLoss >= 0) "+" else "",
            currencyFormatter.format(overallGainLoss)
        )
        binding.totalOverallGainLossTextView.setTextColor(if (overallGainLoss >= 0) Color.parseColor("#4CAF50") else Color.parseColor("#F44336"))

        binding.totalOverallPercentageChangeTextView.text = String.format(Locale.getDefault(), "%.2f%%", overallPercentageChange)
        binding.totalOverallPercentageChangeTextView.setTextColor(if (overallGainLoss >= 0) Color.parseColor("#4CAF50") else Color.parseColor("#F44336"))
    }

    // Removed the generateDummyHoldings() function as data now comes from ViewModel

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// NEW: ViewModel Factory to create DashboardViewModel with a repository dependency
// This class is defined inside DashboardFragment for simplicity, but could be a top-level class.
class DashboardViewModelFactory(private val repository: MutualFundRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}