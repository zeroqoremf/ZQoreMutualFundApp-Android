package com.zeroqore.mutualfundapp.ui.portfolio

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.zeroqore.mutualfundapp.MutualFundApplication
import com.zeroqore.mutualfundapp.data.AssetAllocation
import com.zeroqore.mutualfundapp.data.PortfolioDisplayItem
import com.zeroqore.mutualfundapp.data.PortfolioSummary
import com.zeroqore.mutualfundapp.databinding.FragmentPortfolioBinding
import java.text.NumberFormat
import java.util.Locale

class PortfolioFragment : Fragment() {

    private var _binding: FragmentPortfolioBinding? = null
    private val binding get() = _binding!!

    private lateinit var portfolioViewModel: PortfolioViewModel
    private lateinit var assetAllocationAdapter: AssetAllocationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPortfolioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val application = requireActivity().application as MutualFundApplication
        portfolioViewModel = ViewModelProvider(
            this,
            // MODIFIED: Pass both mutualFundRepository AND authTokenManager to the Factory
            PortfolioViewModel.Factory(
                application.container.mutualFundRepository,
                application.container.authTokenManager // ADDED: Pass AuthTokenManager
            )
        ).get(PortfolioViewModel::class.java)

        assetAllocationAdapter = AssetAllocationAdapter { fundType ->
            portfolioViewModel.toggleAssetTypeExpansion(fundType)
        }
        binding.assetAllocationRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = assetAllocationAdapter
            setHasFixedSize(true)
        }

        portfolioViewModel.portfolioSummary.observe(viewLifecycleOwner) { summary ->
            if (summary != null) {
                updatePortfolioSummaryUI(summary)
            } else {
                Log.w("PortfolioFragment", "PortfolioSummary is null, not updating UI.")
            }
        }

        portfolioViewModel.portfolioDisplayItems.observe(viewLifecycleOwner) { items ->
            if (items != null) {
                assetAllocationAdapter.submitList(items)
                Log.d("PortfolioFragment", "PortfolioDisplayItems updated. Count: ${items.size}")
                val allExpanded = items.filterIsInstance<PortfolioDisplayItem.AssetTypeHeader>().all { it.isExpanded }
                binding.expandCollapseAllTextView.text = if (allExpanded) "Collapse All" else "Expand All"

            } else {
                Log.w("PortfolioFragment", "PortfolioDisplayItems is null, not updating RecyclerView.")
                assetAllocationAdapter.submitList(emptyList())
            }
        }

        portfolioViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.swipeRefreshLayout.isRefreshing = isLoading

            binding.portfolioContentGroup.visibility =
                if (isLoading && (portfolioViewModel.portfolioSummary.value == null && portfolioViewModel.portfolioDisplayItems.value.isNullOrEmpty())) View.GONE else View.VISIBLE

            binding.refreshFab.isEnabled = !isLoading
        }

        portfolioViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrBlank()) {
                binding.errorMessageTextView.text = errorMessage
                binding.errorMessageTextView.visibility = View.VISIBLE
                if (portfolioViewModel.portfolioSummary.value == null && portfolioViewModel.portfolioDisplayItems.value.isNullOrEmpty()) {
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

        binding.expandCollapseAllTextView.setOnClickListener {
            val currentText = binding.expandCollapseAllTextView.text.toString()
            if (currentText == "Expand All") {
                portfolioViewModel.toggleAllAssetTypes(true)
            } else {
                portfolioViewModel.toggleAllAssetTypes(false)
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
            "%s%s (%s)",
            if (summary.overallGainLoss >= 0) "+" else "",
            currencyFormatter.format(summary.overallGainLoss),
            percentFormatter.format(summary.overallGainLossPercentage / 100.0)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}