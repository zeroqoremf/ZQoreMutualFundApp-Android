// app/src/main/java/com/zeroqore/mutualfundapp/ui/dashboard/DashboardFragment.kt
package com.zeroqore.mutualfundapp.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.zeroqore.mutualfundapp.data.MutualFundHolding
import com.zeroqore.mutualfundapp.databinding.FragmentDashboardBinding
import java.text.NumberFormat
import java.util.Locale

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

        val holdingsList = createStubHoldings()
        setupRecyclerView(holdingsList)
        calculateAndDisplayPortfolioSummary(holdingsList)
    }

    private fun setupRecyclerView(holdingsList: List<MutualFundHolding>) {
        val adapter = MutualFundHoldingAdapter(holdingsList) { mutualFund ->
            val action = DashboardFragmentDirections.actionDashboardFragmentToFundDetailFragment(mutualFund)
            findNavController().navigate(action)
        }
        binding.fundHoldingsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.fundHoldingsRecyclerView.adapter = adapter
    }

    private fun calculateAndDisplayPortfolioSummary(holdings: List<MutualFundHolding>) {
        var totalCurrentValue = 0.0
        var totalInvestedValue = 0.0
        var totalPreviousDayValue = 0.0 // Variable for "Today's Gain/Loss" calculation

        for (holding in holdings) {
            totalCurrentValue += holding.currentValue
            totalInvestedValue += holding.purchasePrice
            // Calculate previous day's total value for all units based on previousDayNav
            totalPreviousDayValue += (holding.previousDayNav * holding.units)
        }

        val totalOverallGainLossAbsolute = totalCurrentValue - totalInvestedValue
        val totalOverallPercentageChange = if (totalInvestedValue != 0.0) {
            (totalOverallGainLossAbsolute / totalInvestedValue) * 100.0
        } else {
            0.0
        }

        // New calculations for "Today's Gain/Loss"
        val totalTodayGainLossAbsolute = totalCurrentValue - totalPreviousDayValue
        val totalTodayGainLossPercentage = if (totalPreviousDayValue != 0.0) {
            (totalTodayGainLossAbsolute / totalPreviousDayValue) * 100.0
        } else {
            0.0
        }


        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        val percentFormatter = NumberFormat.getPercentInstance(Locale.getDefault())
        percentFormatter.minimumFractionDigits = 2
        percentFormatter.maximumFractionDigits = 2

        binding.totalInvestedValueTextView.text = currencyFormatter.format(totalInvestedValue)
        binding.totalCurrentValueTextView.text = currencyFormatter.format(totalCurrentValue)

        binding.totalOverallGainLossTextView.text = currencyFormatter.format(totalOverallGainLossAbsolute)
        binding.totalOverallPercentageChangeTextView.text = percentFormatter.format(totalOverallPercentageChange / 100.0)

        // Set text for Today's Gain/Loss (USING NEW BINDING IDs)
        binding.todayAbsGainLossTextView.text = currencyFormatter.format(totalTodayGainLossAbsolute) // <-- NEW ID
        binding.todayPctChangeTextView.text = percentFormatter.format(totalTodayGainLossPercentage / 100.0) // <-- NEW ID


        // Apply color to Overall Gain/Loss
        val overallGainLossColor = if (totalOverallGainLossAbsolute >= 0) {
            Color.GREEN
        } else {
            Color.RED
        }
        binding.totalOverallGainLossTextView.setTextColor(overallGainLossColor)
        binding.totalOverallPercentageChangeTextView.setTextColor(overallGainLossColor)

        // Apply color to Today's Gain/Loss (USING NEW BINDING IDs)
        val todayGainLossColor = if (totalTodayGainLossAbsolute >= 0) {
            Color.GREEN
        } else {
            Color.RED
        }
        binding.todayAbsGainLossTextView.setTextColor(todayGainLossColor) // <-- NEW ID
        binding.todayPctChangeTextView.setTextColor(todayGainLossColor) // <-- NEW ID
    }

    private fun createStubHoldings(): List<MutualFundHolding> {
        return listOf(
            MutualFundHolding(
                fundName = "Axis Bluechip Fund - Growth",
                isin = "INF846K01026",
                currentValue = 15000.00,
                purchasePrice = 12000.00,
                units = 100.0,
                currentNav = 150.0,
                purchaseNav = 120.0,
                lastUpdated = "2024-05-29",
                fundType = "Equity",
                category = "Large Cap",
                riskLevel = "High",
                previousDayNav = 149.50 // Added previousDayNav
            ),
            MutualFundHolding(
                fundName = "SBI Small Cap Fund - Regular Growth",
                isin = "INF200K01006",
                currentValue = 8000.00,
                purchasePrice = 9000.00,
                units = 50.0,
                currentNav = 160.0,
                purchaseNav = 180.0,
                lastUpdated = "2024-05-29",
                fundType = "Equity",
                category = "Small Cap",
                riskLevel = "Very High",
                previousDayNav = 161.20 // Added previousDayNav
            ),
            MutualFundHolding(
                fundName = "ICICI Prudential Balanced Advantage Fund",
                isin = "INF194K01089",
                currentValue = 25000.00,
                purchasePrice = 20000.00,
                units = 200.0,
                currentNav = 125.0,
                purchaseNav = 100.0,
                lastUpdated = "2024-05-29",
                fundType = "Hybrid",
                category = "Balanced Advantage",
                riskLevel = "Moderate",
                previousDayNav = 124.80 // Added previousDayNav
            ),
            MutualFundHolding(
                fundName = "HDFC Liquid Fund - Growth",
                isin = "INF179KC1391",
                currentValue = 10500.00,
                purchasePrice = 10000.00,
                units = 100.0,
                currentNav = 105.0,
                purchaseNav = 100.0,
                lastUpdated = "2024-05-29",
                fundType = "Debt",
                category = "Liquid",
                riskLevel = "Low",
                previousDayNav = 104.95 // Added previousDayNav
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}