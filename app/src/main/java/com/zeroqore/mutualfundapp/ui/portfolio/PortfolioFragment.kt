// app/src/main/java/com/zeroqore/mutualfundapp/ui/portfolio/PortfolioFragment.kt
package com.zeroqore.mutualfundapp.ui.portfolio

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController // Although not used here, keep if used elsewhere
import com.zeroqore.mutualfundapp.MutualFundApplication
import com.zeroqore.mutualfundapp.data.AssetAllocation
import com.zeroqore.mutualfundapp.data.PortfolioSummary
import com.zeroqore.mutualfundapp.databinding.FragmentPortfolioBinding
import java.text.NumberFormat
import java.util.Locale

class PortfolioFragment : Fragment() {

    private var _binding: FragmentPortfolioBinding? = null
    private val binding get() = _binding!!

    // UPDATED: Initialize ViewModel using the global ViewModelFactory from your Application class
    private val portfolioViewModel: PortfolioViewModel by viewModels {
        // Access the viewModelFactory property from your custom Application class
        (activity?.application as MutualFundApplication).viewModelFactory
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
            if (summary != null) {
                updatePortfolioSummaryUI(summary)
            } else {
                Log.w("PortfolioFragment", "PortfolioSummary is null, not updating UI.")
            }
        }

        portfolioViewModel.assetAllocation.observe(viewLifecycleOwner) { allocation ->
            if (allocation != null) {
                updateAssetAllocationUI(allocation)
            } else {
                Log.w("PortfolioFragment", "AssetAllocation is null, not updating UI.")
            }
        }

        // ADDED: OBSERVE isLoading state
        portfolioViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.swipeRefreshLayout.isRefreshing = isLoading // Control SwipeRefreshLayout's spinner

            // You might want to hide/show main content based on loading, similar to Dashboard
            // For example, hide content only if loading AND no data is present yet
            binding.portfolioContentGroup.visibility =
                if (isLoading && portfolioViewModel.portfolioSummary.value == null) View.GONE else View.VISIBLE

            binding.refreshFab.isEnabled = !isLoading // Disable FAB while loading
        }

        // ADDED: OBSERVE errorMessage state
        portfolioViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrBlank()) {
                binding.errorMessageTextView.text = errorMessage
                binding.errorMessageTextView.visibility = View.VISIBLE
                // Optional: Hide main content if there's an error and no data
                if (portfolioViewModel.portfolioSummary.value == null) {
                    binding.portfolioContentGroup.visibility = View.GONE
                }
            } else {
                binding.errorMessageTextView.visibility = View.GONE
            }
        }

        // ADDED: Set up SwipeRefreshLayout listener
        binding.swipeRefreshLayout.setOnRefreshListener {
            portfolioViewModel.refreshPortfolioData()
        }

        // ADDED: Set up Floating Action Button listener
        binding.refreshFab.setOnClickListener {
            portfolioViewModel.refreshPortfolioData()
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
        // Also update last updated timestamp if you have a TextView for it
        binding.lastUpdatedTextView.text = "Last Updated: ${summary.lastUpdated}"
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

// REMOVED: The duplicate PortfolioViewModelFactory class is removed from here.
// It is now managed centrally in MutualFundViewModelFactory.kt