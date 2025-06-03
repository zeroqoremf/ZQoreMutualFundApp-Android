// app/src/main/java/com/zeroqore.mutualfundapp/ui/dashboard/DashboardFragment.kt
package com.zeroqore.mutualfundapp.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
// REMOVED: import androidx.fragment.app.viewModels // No longer needed
import androidx.lifecycle.ViewModelProvider // Needed for ViewModelProvider.Factory type
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.zeroqore.mutualfundapp.MutualFundApplication // Import your Application class
import com.zeroqore.mutualfundapp.data.MutualFundHolding
import com.zeroqore.mutualfundapp.databinding.FragmentDashboardBinding
import com.zeroqore.mutualfundapp.ui.dashboard.MutualFundHoldingsAdapter // Assuming your adapter is here or import it
import java.text.NumberFormat
import java.util.Locale
import com.zeroqore.mutualfundapp.ui.dashboard.DashboardViewModel

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    // CHANGED: Initialize ViewModel using explicit ViewModelProvider later
    private lateinit var dashboardViewModel: DashboardViewModel

    // Initialize adapter for RecyclerView
    private lateinit var holdingsAdapter: MutualFundHoldingsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ADDED: Initialize ViewModel here using the custom Factory from your Application class
        val application = requireActivity().application as MutualFundApplication
        dashboardViewModel = ViewModelProvider(
            this,
            DashboardViewModel.Factory(application.container.mutualFundRepository)
        ).get(DashboardViewModel::class.java)

        // Initialize RecyclerView adapter
        // Pass an empty list initially, it will be updated by LiveData
        // The adapter no longer takes the list in its constructor; ListAdapter handles it via submitList
        holdingsAdapter = MutualFundHoldingsAdapter { clickedHolding ->
            val action = DashboardFragmentDirections.actionDashboardFragmentToFundDetailFragment(clickedHolding)
            findNavController().navigate(action)
        }

        binding.fundHoldingsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.fundHoldingsRecyclerView.adapter = holdingsAdapter

        // --- OBSERVE LiveData from ViewModel ---

        dashboardViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Use the renamed ID: progress_bar_dashboard
            binding.progressBarDashboard.visibility = if (isLoading) View.VISIBLE else View.GONE

            // Use the renamed ID: swipe_refresh_layout_dashboard
            binding.swipeRefreshLayoutDashboard.isRefreshing = isLoading

            // While loading, hide error/empty messages and content, unless content is already loaded
            if (isLoading) {
                binding.textErrorDashboard.visibility = View.GONE
                binding.textEmptyDashboard.visibility = View.GONE
                // Only hide RecyclerView if it's loading AND there's no data already
                // Otherwise, keep data visible while refreshing in background
                if (dashboardViewModel.fundHoldings.value.isNullOrEmpty()) {
                    binding.portfolioSummaryCard.visibility = View.GONE
                    binding.fundHoldingsRecyclerView.visibility = View.GONE
                }
            }
            // Optional: Disable refresh button while loading
            binding.refreshFab.isEnabled = !isLoading
        }

        dashboardViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrBlank()) {
                // Use the renamed ID: text_error_dashboard
                binding.textErrorDashboard.text = errorMessage
                binding.textErrorDashboard.visibility = View.VISIBLE
                // Hide other content on error
                binding.progressBarDashboard.visibility = View.GONE // Ensure progress bar is hidden
                binding.textEmptyDashboard.visibility = View.GONE
                binding.portfolioSummaryCard.visibility = View.GONE
                binding.fundHoldingsRecyclerView.visibility = View.GONE
            } else {
                binding.textErrorDashboard.visibility = View.GONE // Hide error if message is null/blank
            }
        }

        dashboardViewModel.fundHoldings.observe(viewLifecycleOwner) { holdings ->
            // Always hide error/loading when new data (or empty list) is received
            binding.textErrorDashboard.visibility = View.GONE
            binding.progressBarDashboard.visibility = View.GONE

            if (holdings.isNullOrEmpty()) {
                // Use the new ID: text_empty_dashboard
                binding.textEmptyDashboard.visibility = View.VISIBLE
                binding.portfolioSummaryCard.visibility = View.GONE // Hide summary if no holdings
                binding.fundHoldingsRecyclerView.visibility = View.GONE // Hide RecyclerView if empty
            } else {
                binding.textEmptyDashboard.visibility = View.GONE
                binding.portfolioSummaryCard.visibility = View.VISIBLE // Show summary if data exists
                binding.fundHoldingsRecyclerView.visibility = View.VISIBLE // Show RecyclerView if data exists
                // Update your RecyclerView adapter with the new holdings list
                holdingsAdapter.submitList(holdings) // Use submitList if using ListAdapter or update your custom adapter
                // --- ENSURE THIS LINE IS PRESENT ---
                updatePortfolioSummary(holdings)
            }
            Log.d("DashboardFragment", "Observed ${holdings?.size} fund holdings.")
        }

        // --- Set up Refresh Mechanisms ---

        // Set up SwipeRefreshLayout listener for pull-to-refresh
        binding.swipeRefreshLayoutDashboard.setOnRefreshListener {
            // This will trigger the isLoading LiveData to true, which will show the spinner
            dashboardViewModel.refreshHoldings()
        }

        // Set up Floating Action Button listener for explicit refresh
        binding.refreshFab.setOnClickListener {
            dashboardViewModel.refreshHoldings()
        }
    }

    private fun updatePortfolioSummary(holdings: List<MutualFundHolding>) {
        Log.d("PortfolioSummaryDebug", "Starting updatePortfolioSummary. Holdings count: ${holdings.size}")

        var totalInvested = 0.0
        var totalCurrentValue = 0.0

        for (holding in holdings) {
            Log.d("PortfolioSummaryDebug", "Processing holding: ${holding.fundName}")
            Log.d("PortfolioSummaryDebug", "  purchasePrice: ${holding.purchasePrice}, units: ${holding.units}, currentValue: ${holding.currentValue}")
            totalInvested += (holding.purchasePrice * holding.units)
            totalCurrentValue += holding.currentValue
        }

        val overallGainLoss = totalCurrentValue - totalInvested
        val overallPercentageChange = if (totalInvested != 0.0) {
            (overallGainLoss / totalInvested) * 100
        } else {
            0.0
        }

        Log.d("PortfolioSummaryDebug", "Calculated totalInvested: $totalInvested")
        Log.d("PortfolioSummaryDebug", "Calculated totalCurrentValue: $totalCurrentValue")
        Log.d("PortfolioSummaryDebug", "Calculated overallGainLoss: $overallGainLoss")
        Log.d("PortfolioSummaryDebug", "Calculated overallPercentageChange: $overallPercentageChange")

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}