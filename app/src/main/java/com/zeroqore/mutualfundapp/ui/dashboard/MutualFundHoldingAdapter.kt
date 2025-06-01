// app/src/main/java/com/zeroqore/mutualfundapp/ui/dashboard/MutualFundHoldingAdapter.kt
package com.zeroqore.mutualfundapp.ui.dashboard

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zeroqore.mutualfundapp.data.MutualFundHolding
import com.zeroqore.mutualfundapp.databinding.ItemMutualFundHoldingBinding // This will be generated
import java.text.NumberFormat
import java.util.Locale

class MutualFundHoldingAdapter(
    private val holdings: List<MutualFundHolding>,
    private val onItemClick: (MutualFundHolding) -> Unit // Click listener
) : RecyclerView.Adapter<MutualFundHoldingAdapter.HoldingViewHolder>() {

    // ViewHolder class for caching view components
    inner class HoldingViewHolder(private val binding: ItemMutualFundHoldingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Bind data to the views in the item layout
        fun bind(holding: MutualFundHolding) {
            binding.fundNameTextView.text = holding.fundName

            val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
            val percentFormatter = NumberFormat.getPercentInstance(Locale.getDefault())
            percentFormatter.minimumFractionDigits = 2
            percentFormatter.maximumFractionDigits = 2

            binding.currentValueTextView.text = currencyFormatter.format(holding.currentValue)

            val gainLossAbsolute = holding.currentValue - holding.purchasePrice
            val gainLossPercentage = if (holding.purchasePrice != 0.0) {
                (gainLossAbsolute / holding.purchasePrice) * 100.0
            } else {
                0.0
            }

            // Format gain/loss text
            val gainLossText = "${currencyFormatter.format(gainLossAbsolute)} (${percentFormatter.format(gainLossPercentage / 100.0)})"
            binding.gainLossTextView.text = gainLossText

            // Set color for gain/loss text
            val gainLossColor = if (gainLossAbsolute >= 0) Color.GREEN else Color.RED
            binding.gainLossTextView.setTextColor(gainLossColor)

            binding.unitsTextView.text = String.format(Locale.getDefault(), "%.2f", holding.units)
            binding.lastUpdatedTextView.text = "Last updated: ${holding.lastUpdated}"

            // Set click listener for the entire item
            binding.root.setOnClickListener { onItemClick(holding) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HoldingViewHolder {
        // Inflate the item layout using View Binding
        val binding = ItemMutualFundHoldingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HoldingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HoldingViewHolder, position: Int) {
        // Bind data for the item at the current position
        holder.bind(holdings[position])
    }

    override fun getItemCount(): Int = holdings.size // Return the total number of items
}