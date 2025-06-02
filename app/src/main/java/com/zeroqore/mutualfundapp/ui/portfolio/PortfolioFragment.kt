// app/src/main/java/com/zeroqore/mutualfundapp/ui/portfolio/PortfolioFragment.kt
package com.zeroqore.mutualfundapp.ui.portfolio

import android.graphics.Color
import android.os.Bundle
import android.util.Log // Import Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zeroqore.mutualfundapp.MutualFundApplication
import com.zeroqore.mutualfundapp.data.AssetAllocation
import com.zeroqore.mutualfundapp.data.MutualFundAppRepository
import com.zeroqore.mutualfundapp.data.PortfolioSummary
import com.zeroqore.mutualfundapp.databinding.FragmentPortfolioBinding
import java.text.NumberFormat
import java.util.Locale

class PortfolioFragment : Fragment() {

    private var _binding: FragmentPortfolioBinding? = null
    private val binding get() = _binding!!

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

        portfolioViewModel.portfolioSummary.observe(viewLifecycleOwner) { summary ->
            if (summary != null) { // Add null check for safety
                updatePortfolioSummaryUI(summary)
            } else {
                Log.w("PortfolioFragment", "PortfolioSummary is null, not updating UI.")
            }
        }

        portfolioViewModel.assetAllocation.observe(viewLifecycleOwner) { allocation ->
            if (allocation != null) { // Add null check for safety
                updateAssetAllocationUI(allocation)
            } else {
                Log.w("PortfolioFragment", "AssetAllocation is null, not updating UI.")
            }
        }
    }

    private fun updatePortfolioSummaryUI(summary: PortfolioSummary) {
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        val percentFormatter = NumberFormat.getPercentInstance(Locale.getDefault())
        percentFormatter.minimumFractionDigits = 2
        percentFormatter.maximumFractionDigits = 2

        val investedText = currencyFormatter.format(summary.totalInvested)
        val currentValueText = currencyFormatter.format(summary.currentValue)
        val gainLossText = String.format(
            Locale.getDefault(),
            "%s%s (%.2f%%)",
            if (summary.overallGainLoss >= 0) "+" else "",
            currencyFormatter.format(summary.overallGainLoss),
            summary.overallGainLossPercentage
        )

        Log.d("PortfolioFragment", "Updating Summary UI:")
        Log.d("PortfolioFragment", "Total Invested: $investedText")
        Log.d("PortfolioFragment", "Current Value: $currentValueText")
        Log.d("PortfolioFragment", "Gain/Loss: $gainLossText")

        binding.totalInvestedValueTextView.text = investedText
        binding.totalCurrentValueTextView.text = currentValueText
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

        val equityValText = String.format(
            Locale.getDefault(),
            "%s (%s)",
            currencyFormatter.format(allocation.equityValue),
            percentFormatter.format(allocation.equityPercentage / 100.0)
        )
        val debtValText = String.format(
            Locale.getDefault(),
            "%s (%s)",
            currencyFormatter.format(allocation.debtValue),
            percentFormatter.format(allocation.debtPercentage / 100.0)
        )
        val hybridValText = String.format(
            Locale.getDefault(),
            "%s (%s)",
            currencyFormatter.format(allocation.hybridValue),
            percentFormatter.format(allocation.hybridPercentage / 100.0)
        )

        Log.d("PortfolioFragment", "Updating Asset Allocation UI:")
        Log.d("PortfolioFragment", "Equity: $equityValText")
        Log.d("PortfolioFragment", "Debt: $debtValText")
        Log.d("PortfolioFragment", "Hybrid: $hybridValText")

        binding.equityValueTextView.text = equityValText
        binding.debtValueTextView.text = debtValText
        binding.hybridValueTextView.text = hybridValText
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class PortfolioViewModelFactory(private val repository: MutualFundAppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PortfolioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PortfolioViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}