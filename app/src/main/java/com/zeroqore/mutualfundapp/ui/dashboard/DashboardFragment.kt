// app/src/main/java/com/zeroqore.mutualfundapp/ui/dashboard/DashboardFragment.kt
package com.zeroqore.mutualfundapp.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider // Still needed for ViewModelProvider.Factory type
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.zeroqore.mutualfundapp.MutualFundApplication // Import your Application class
import com.zeroqore.mutualfundapp.data.MutualFundHolding
import com.zeroqore.mutualfundapp.databinding.FragmentDashboardBinding
import java.text.NumberFormat
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    // Initialize ViewModel using the global ViewModelFactory from your Application class
    private val dashboardViewModel: DashboardViewModel by viewModels {
        // Access the viewModelFactory property from your custom Application class
        (activity?.application as MutualFundApplication).viewModelFactory
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
            setupRecyclerView(holdings)
            updatePortfolioSummary(holdings)
        }

        // OBSERVE isLoading state to control ProgressBar and SwipeRefreshLayout
        dashboardViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.swipeRefreshLayout.isRefreshing = isLoading // Control SwipeRefreshLayout's spinner

            // Only hide RecyclerView if it's loading AND there's no data already
            // Otherwise, keep data visible while refreshing in background
            binding.fundHoldingsRecyclerView.visibility =
                if (isLoading && dashboardViewModel.fundHoldings.value.isNullOrEmpty()) View.GONE else View.VISIBLE

            // Optional: Disable refresh button while loading
            binding.refreshFab.isEnabled = !isLoading
        }

        // OBSERVE errorMessage state
        dashboardViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrBlank()) {
                binding.errorMessageTextView.text = errorMessage
                binding.errorMessageTextView.visibility = View.VISIBLE
                // If there's an error and no data, hide RecyclerView
                if (dashboardViewModel.fundHoldings.value.isNullOrEmpty()) {
                    binding.fundHoldingsRecyclerView.visibility = View.GONE
                }
            } else {
                binding.errorMessageTextView.visibility = View.GONE
            }
        }

        // Set up SwipeRefreshLayout listener for pull-to-refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            dashboardViewModel.refreshHoldings()
        }

        // Set up Floating Action Button listener for explicit refresh
        binding.refreshFab.setOnClickListener {
            dashboardViewModel.refreshHoldings()
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}