// app/src/main/java/com/zeroqore/mutualfundapp/ui/dashboard/MutualFundHoldingsAdapter.kt
package com.zeroqore.mutualfundapp.ui.dashboard

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zeroqore.mutualfundapp.data.MutualFundHolding
import com.zeroqore.mutualfundapp.databinding.ItemMutualFundHoldingBinding
import java.text.NumberFormat
import java.util.Locale
// Removed SimpleDateFormat import as lastUpdated is String

class MutualFundHoldingsAdapter(
    private val holdings: List<MutualFundHolding>,
    private val onItemClick: (MutualFundHolding) -> Unit
) : RecyclerView.Adapter<MutualFundHoldingsAdapter.MutualFundHoldingViewHolder>() {

    inner class MutualFundHoldingViewHolder(private val binding: ItemMutualFundHoldingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(holdings[position])
                }
            }
        }

        fun bind(holding: MutualFundHolding) {
            binding.fundNameTextView.text = holding.fundName

            val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())

            binding.currentValueTextView.text = currencyFormatter.format(holding.currentValue)
            binding.unitsTextView.text = String.format(Locale.getDefault(), "%.2f", holding.units)

            // --- IMPORTANT CHANGE: Using 'purchasePrice' instead of 'investedValue' ---
            val gainLoss = holding.currentValue - holding.purchasePrice
            val percentageChange = if (holding.purchasePrice != 0.0) {
                (gainLoss / holding.purchasePrice) * 100
            } else {
                0.0
            }

            binding.gainLossTextView.text = String.format(
                Locale.getDefault(),
                "%s%s (%.2f%%)",
                if (gainLoss >= 0) "+" else "",
                currencyFormatter.format(gainLoss),
                percentageChange
            )

            val gainLossColor = if (gainLoss >= 0) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
            binding.gainLossTextView.setTextColor(gainLossColor)

            // --- IMPORTANT CHANGE: Direct use of 'lastUpdated' as String ---
            binding.lastUpdatedTextView.text = "Last updated: ${holding.lastUpdated}"
        }

        // Removed formatDate helper function as lastUpdated is already a String
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MutualFundHoldingViewHolder {
        val binding = ItemMutualFundHoldingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MutualFundHoldingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MutualFundHoldingViewHolder, position: Int) {
        holder.bind(holdings[position])
    }

    override fun getItemCount(): Int = holdings.size
}