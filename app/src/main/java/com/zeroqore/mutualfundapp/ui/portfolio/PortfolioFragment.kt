// app/src/main/java/com/zeroqore/mutualfundapp/ui/portfolio/PortfolioFragment.kt
package com.zeroqore.mutualfundapp.ui.portfolio

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
// REMOVED: import androidx.fragment.app.viewModels // No longer needed for explicit ViewModelProvider
import androidx.lifecycle.ViewModelProvider // Added this import
import androidx.navigation.fragment.findNavController
import com.zeroqore.mutualfundapp.MutualFundApplication
import com.zeroqore.mutualfundapp.data.AssetAllocation
import com.zeroqore.mutualfundapp.data.PortfolioSummary
import com.zeroqore.mutualfundapp.databinding.FragmentPortfolioBinding
import java.text.NumberFormat
import java.util.Locale

class PortfolioFragment : Fragment() {

    private var _binding: FragmentPortfolioBinding? = null
    private val binding get() = _binding!!

    // CHANGED: Initialize ViewModel using explicit ViewModelProvider later
    private lateinit var portfolioViewModel: PortfolioViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPortfolioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ADDED: Initialize ViewModel here using the custom Factory from your Application class
        val application = requireActivity().application as MutualFundApplication
        portfolioViewModel = ViewModelProvider(
            this,
            PortfolioViewModel.Factory(application.container.mutualFundRepository)
        ).get(PortfolioViewModel::class.java)

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

        portfolioViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.swipeRefreshLayout.isRefreshing = isLoading

            binding.portfolioContentGroup.visibility =
                if (isLoading && portfolioViewModel.portfolioSummary.value == null) View.GONE else View.VISIBLE

            binding.refreshFab.isEnabled = !isLoading
        }

        portfolioViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrBlank()) {
                binding.errorMessageTextView.text = errorMessage
                binding.errorMessageTextView.visibility = View.VISIBLE
                if (portfolioViewModel.portfolioSummary.value == null) {
                    binding.portfolioContentGroup.visibility = View.GONE
                }
            } else {
                binding.errorMessageTextView.visibility = View.GONE
            }
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            portfolioViewModel.refreshPortfolioData()
        }

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