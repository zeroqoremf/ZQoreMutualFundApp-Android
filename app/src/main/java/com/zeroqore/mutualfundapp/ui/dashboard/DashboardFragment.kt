// app/src/main/java/com/zeroqore/mutualfundapp/ui/dashboard/DashboardFragment.kt
package com.zeroqore.mutualfundapp.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.zeroqore.mutualfundapp.data.MutualFundHolding // Make sure this import is correct
import com.zeroqore.mutualfundapp.databinding.FragmentDashboardBinding // Make sure this import is correct
import java.text.NumberFormat
import java.util.Date
import java.util.Locale
import android.graphics.Color // For gain/loss color

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Prepare Dummy Data (Replace with real data from ViewModel later)
        val fundHoldings = generateDummyHoldings()

        // 2. Setup RecyclerView
        setupRecyclerView(fundHoldings)

        // 3. Update Portfolio Summary (Based on dummy data for now)
        updatePortfolioSummary(fundHoldings)
    }

    private fun setupRecyclerView(holdings: List<MutualFundHolding>) {
        val adapter = MutualFundHoldingsAdapter(holdings) { clickedHolding ->
            // Handle item click: Navigate to FundDetailFragment
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
            totalInvested += (holding.purchasePrice * holding.units) // Assuming purchasePrice * units is invested value
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

        // For simplicity, today's gain/loss is just overall for now in dummy data
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

    // Dummy data generator (will be replaced by live data from API later)
    private fun generateDummyHoldings(): List<MutualFundHolding> {
        return listOf(
            MutualFundHolding(
                fundName = "Axis Bluechip Fund - Growth",
                isin = "INF846K01802",
                currentValue = 15000.0,
                purchasePrice = 12000.0,
                units = 100.0,
                currentNav = 150.0,
                purchaseNav = 120.0,
                lastUpdated = "2024-05-29",
                fundType = "Equity",
                category = "Large Cap",
                riskLevel = "High",
                previousDayNav = 148.0
            ),
            MutualFundHolding(
                fundName = "SBI Small Cap Fund - Growth",
                isin = "INF200K01673",
                currentValue = 25000.0,
                purchasePrice = 28000.0,
                units = 50.0,
                currentNav = 500.0,
                purchaseNav = 560.0,
                lastUpdated = "2024-05-29",
                fundType = "Equity",
                category = "Small Cap",
                riskLevel = "Very High",
                previousDayNav = 510.0
            ),
            MutualFundHolding(
                fundName = "ICICI Prudential Balanced Advantage Fund - Growth",
                isin = "INF761K01912",
                currentValue = 8000.0,
                purchasePrice = 7500.0,
                units = 80.0,
                currentNav = 100.0,
                purchaseNav = 93.75,
                lastUpdated = "2024-05-29",
                fundType = "Hybrid",
                category = "Dynamic Asset Allocation",
                riskLevel = "Moderate",
                previousDayNav = 99.0
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}