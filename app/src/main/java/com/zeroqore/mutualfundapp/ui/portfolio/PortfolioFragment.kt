// app/src/main/java/com/zeroqore/mutualfundapp/ui/portfolio/PortfolioFragment.kt
package com.zeroqore.mutualfundapp.ui.portfolio

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // NEW IMPORT
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider // NEW IMPORT
import com.zeroqore.mutualfundapp.MutualFundApplication // NEW IMPORT
import com.zeroqore.mutualfundapp.data.AssetAllocation // NEW IMPORT
import com.zeroqore.mutualfundapp.data.MutualFundRepository // NEW IMPORT
import com.zeroqore.mutualfundapp.data.PortfolioSummary // NEW IMPORT
import com.zeroqore.mutualfundapp.databinding.FragmentPortfolioBinding // Make sure this import is correct
import java.text.NumberFormat
import java.util.Locale

class PortfolioFragment : Fragment() {

    private var _binding: FragmentPortfolioBinding? = null
    private val binding get() = _binding!!

    // Initialize ViewModel using ViewModelProvider.Factory
    private val portfolioViewModel: PortfolioViewModel by viewModels {
        PortfolioViewModelFactory((activity?.application as MutualFundApplication).appContainer.mutualFundRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPortfolioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe portfolio summary from the ViewModel
        portfolioViewModel.portfolioSummary.observe(viewLifecycleOwner) { summary ->
            updatePortfolioSummaryUI(summary)
        }

        // Observe asset allocation from the ViewModel
        portfolioViewModel.assetAllocation.observe(viewLifecycleOwner) { allocation ->
            updateAssetAllocationUI(allocation)
        }
    }

    private fun updatePortfolioSummaryUI(summary: PortfolioSummary) {
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        val percentFormatter = NumberFormat.getPercentInstance(Locale.getDefault())
        percentFormatter.minimumFractionDigits = 2
        percentFormatter.maximumFractionDigits = 2

        binding.totalInvestedValueTextView.text = currencyFormatter.format(summary.totalInvested)
        binding.totalCurrentValueTextView.text = currencyFormatter.format(summary.totalCurrentValue)

        val gainLossText = String.format(
            Locale.getDefault(),
            "%s%s (%.2f%%)",
            if (summary.overallGainLoss >= 0) "+" else "",
            currencyFormatter.format(summary.overallGainLoss),
            summary.overallPercentageChange
        )
        binding.overallGainLossTextView.text = gainLossText
        binding.overallGainLossTextView.setTextColor(
            if (summary.overallGainLoss >= 0) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
        )
    }

    private fun updateAssetAllocationUI(allocation: AssetAllocation) {
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        val percentFormatter = NumberFormat.getPercentInstance(Locale.getDefault())
        percentFormatter.minimumFractionDigits = 2
        percentFormatter.maximumFractionDigits = 2

        // Equity
        binding.equityValueTextView.text = String.format(
            Locale.getDefault(),
            "%s (%s)",
            currencyFormatter.format(allocation.equityValue),
            percentFormatter.format(allocation.equityPercentage / 100.0) // Convert percentage to fraction for formatter
        )

        // Debt
        binding.debtValueTextView.text = String.format(
            Locale.getDefault(),
            "%s (%s)",
            currencyFormatter.format(allocation.debtValue),
            percentFormatter.format(allocation.debtPercentage / 100.0)
        )

        // Hybrid
        binding.hybridValueTextView.text = String.format(
            Locale.getDefault(),
            "%s (%s)",
            currencyFormatter.format(allocation.hybridValue),
            percentFormatter.format(allocation.hybridPercentage / 100.0)
        )

        // Add more categories here if needed
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// NEW: ViewModel Factory for PortfolioViewModel
class PortfolioViewModelFactory(private val repository: MutualFundRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PortfolioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PortfolioViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}