// app/src/main/java/com/zeroqore/mutualfundapp/ui/funddetail/FundDetailFragment.kt
package com.zeroqore.mutualfundapp.ui.funddetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.zeroqore.mutualfundapp.databinding.FragmentFundDetailBinding
import java.text.NumberFormat
import java.util.Locale

class FundDetailFragment : Fragment() {

    private var _binding: FragmentFundDetailBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    // Use navArgs to get the arguments passed to this fragment
    private val args: FundDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFundDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the MutualFundHolding object from arguments
        val mutualFund = args.mutualFund

        // Formatters for currency and units
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        val unitFormatter = NumberFormat.getNumberInstance(Locale.getDefault())
        unitFormatter.minimumFractionDigits = 2
        unitFormatter.maximumFractionDigits = 4 // Allowing for more precise unit display

        // Populate the TextViews with data from the mutualFund object
        binding.fundNameTextView.text = mutualFund.fundName
        binding.isinTextView.text = mutualFund.isin
        binding.currentValueTextView.text = currencyFormatter.format(mutualFund.currentValue)
        binding.unitsTextView.text = unitFormatter.format(mutualFund.units)
        binding.currentNavTextView.text = unitFormatter.format(mutualFund.currentNav) // NAVs are typically numbers, not currency
        binding.purchaseNavTextView.text = unitFormatter.format(mutualFund.purchaseNav) // NAVs are typically numbers, not currency
        binding.lastUpdatedTextView.text = mutualFund.lastUpdated

        // You can add more UI elements and logic here for other properties like fundType, category, riskLevel etc.
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}