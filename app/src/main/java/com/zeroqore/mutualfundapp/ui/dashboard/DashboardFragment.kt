// app/src/main/java/com/zeroqore.mutualfundapp/ui/dashboard/DashboardFragment.kt
package com.zeroqore.mutualfundapp.ui.dashboard

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
import com.zeroqore.mutualfundapp.data.MutualFundHolding
import com.zeroqore.mutualfundapp.databinding.FragmentDashboardBinding
import com.zeroqore.mutualfundapp.ui.dashboard.MutualFundHoldingsAdapter
import java.text.NumberFormat
import java.util.Locale
// No need to import DashboardViewModel again, it's in the same package

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var dashboardViewModel: DashboardViewModel
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

        val application = requireActivity().application as MutualFundApplication
        dashboardViewModel = ViewModelProvider(
            this,
            DashboardViewModel.Factory(application.container.mutualFundRepository)
        ).get(DashboardViewModel::class.java)

        holdingsAdapter = MutualFundHoldingsAdapter { clickedHolding ->
            val action = DashboardFragmentDirections.actionDashboardFragmentToFundDetailFragment(clickedHolding)
            findNavController().navigate(action)
        }

        binding.fundHoldingsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.fundHoldingsRecyclerView.adapter = holdingsAdapter

        dashboardViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarDashboard.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.swipeRefreshLayoutDashboard.isRefreshing = isLoading

            if (isLoading) {
                binding.textErrorDashboard.visibility = View.GONE
                binding.textEmptyDashboard.visibility = View.GONE
                if (dashboardViewModel.fundHoldings.value.isNullOrEmpty()) {
                    binding.portfolioSummaryCard.visibility = View.GONE
                    binding.fundHoldingsRecyclerView.visibility = View.GONE
                }
            }
            binding.refreshFab.isEnabled = !isLoading
        }

        dashboardViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrBlank()) {
                binding.textErrorDashboard.text = errorMessage
                binding.textErrorDashboard.visibility = View.VISIBLE
                binding.progressBarDashboard.visibility = View.GONE
                binding.textEmptyDashboard.visibility = View.GONE
                binding.portfolioSummaryCard.visibility = View.GONE
                binding.fundHoldingsRecyclerView.visibility = View.GONE
            } else {
                binding.textErrorDashboard.visibility = View.GONE
            }
        }

        dashboardViewModel.fundHoldings.observe(viewLifecycleOwner) { holdings ->
            binding.textErrorDashboard.visibility = View.GONE
            binding.progressBarDashboard.visibility = View.GONE

            if (holdings.isNullOrEmpty()) {
                binding.textEmptyDashboard.visibility = View.VISIBLE
                binding.portfolioSummaryCard.visibility = View.GONE
                binding.fundHoldingsRecyclerView.visibility = View.GONE
            } else {
                binding.textEmptyDashboard.visibility = View.GONE
                binding.portfolioSummaryCard.visibility = View.VISIBLE
                binding.fundHoldingsRecyclerView.visibility = View.VISIBLE
                holdingsAdapter.submitList(holdings)
                updatePortfolioSummary(holdings)
            }
            Log.d("DashboardFragment", "Observed ${holdings?.size} fund holdings.")
        }

        binding.swipeRefreshLayoutDashboard.setOnRefreshListener {
            dashboardViewModel.refreshHoldings()
        }

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
            // FIX: Correctly calculate totalInvested. Assumes purchasePrice in JSON is the total invested per fund.
            totalInvested += holding.purchasePrice // CHANGED from (holding.purchasePrice * holding.units)
            totalCurrentValue += holding.currentValue
        }

        val overallGainLoss = totalCurrentValue - totalInvested
        val overallPercentageChange = if (totalInvested != 0.0) {
            (overallGainLoss / totalInvested) * 100
        } else {
            0.0
        }

        // --- Calculate Today's Gain/Loss (New Section) ---
        var todaysAbsoluteGainLoss = 0.0
        // It's good practice to calculate today's % change based on previous day's total value,
        // but for simplicity, we'll calculate it based on the previous day's NAV relative to current NAV.
        // A more robust app might sum (units * previousDayNav) to get previous total value.
        var totalPreviousDayValue = 0.0 // Added for more accurate daily % change calculation

        for (holding in holdings) {
            val dailyGainLossPerUnit = holding.currentNav - holding.previousDayNav
            val dailyGainLossForFund = dailyGainLossPerUnit * holding.units
            todaysAbsoluteGainLoss += dailyGainLossForFund
            totalPreviousDayValue += (holding.previousDayNav * holding.units) // Sum up previous day's total value
        }

        val todaysPercentageChange = if (totalPreviousDayValue != 0.0) {
            (todaysAbsoluteGainLoss / totalPreviousDayValue) * 100
        } else {
            0.0
        }
        // --- End New Section ---


        Log.d("PortfolioSummaryDebug", "Calculated totalInvested: $totalInvested")
        Log.d("PortfolioSummaryDebug", "Calculated totalCurrentValue: $totalCurrentValue")
        Log.d("PortfolioSummaryDebug", "Calculated overallGainLoss: $overallGainLoss")
        Log.d("PortfolioSummaryDebug", "Calculated overallPercentageChange: $overallPercentageChange")
        Log.d("PortfolioSummaryDebug", "Calculated todaysAbsoluteGainLoss: $todaysAbsoluteGainLoss")
        Log.d("PortfolioSummaryDebug", "Calculated todaysPercentageChange: $todaysPercentageChange")


        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())

        binding.totalInvestedValueTextView.text = currencyFormatter.format(totalInvested)
        binding.totalCurrentValueTextView.text = currencyFormatter.format(totalCurrentValue)

        // Updated these to use the new 'todaysAbsoluteGainLoss' and 'todaysPercentageChange'
        binding.todayAbsGainLossTextView.text = String.format(
            Locale.getDefault(),
            "%s%s",
            if (todaysAbsoluteGainLoss >= 0) "+" else "",
            currencyFormatter.format(todaysAbsoluteGainLoss)
        )
        binding.todayAbsGainLossTextView.setTextColor(if (todaysAbsoluteGainLoss >= 0) Color.parseColor("#4CAF50") else Color.parseColor("#F44336"))

        binding.todayPctChangeTextView.text = String.format(Locale.getDefault(), "%.2f%%", todaysPercentageChange)
        binding.todayPctChangeTextView.setTextColor(if (todaysAbsoluteGainLoss >= 0) Color.parseColor("#4CAF50") else Color.parseColor("#F44336"))

        // These remain for overall gain/loss
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