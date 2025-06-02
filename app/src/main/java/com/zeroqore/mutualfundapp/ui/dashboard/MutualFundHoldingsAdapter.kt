// app/src/main/java/com/zeroqore/mutualfundapp/ui/dashboard/MutualFundHoldingsAdapter.kt
package com.zeroqore.mutualfundapp.ui.dashboard

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil // Import DiffUtil
import androidx.recyclerview.widget.ListAdapter // Import ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zeroqore.mutualfundapp.data.MutualFundHolding
import com.zeroqore.mutualfundapp.databinding.ItemMutualFundHoldingBinding
import java.text.NumberFormat
import java.util.Locale

// Change from RecyclerView.Adapter to ListAdapter
class MutualFundHoldingsAdapter(
    private val onItemClick: (MutualFundHolding) -> Unit // Holdings list is now managed by ListAdapter
) : ListAdapter<MutualFundHolding, MutualFundHoldingsAdapter.MutualFundHoldingViewHolder>(MutualFundHoldingDiffCallback()) {

    // MutualFundHoldingViewHolder remains largely the same
    inner class MutualFundHoldingViewHolder(private val binding: ItemMutualFundHoldingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // Use getItem(position) from ListAdapter
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(holding: MutualFundHolding) {
            binding.fundNameTextView.text = holding.fundName

            val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())

            binding.currentValueTextView.text = currencyFormatter.format(holding.currentValue)
            binding.unitsTextView.text = String.format(Locale.getDefault(), "%.2f", holding.units)

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

            binding.lastUpdatedTextView.text = "Last updated: ${holding.lastUpdated}"
        }
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
        // Use getItem(position) from ListAdapter
        holder.bind(getItem(position))
    }

    // You no longer need to override getItemCount() or pass the list in the constructor
    // as ListAdapter handles it internally.

    // NEW: DiffUtil.ItemCallback implementation
    class MutualFundHoldingDiffCallback : DiffUtil.ItemCallback<MutualFundHolding>() {
        override fun areItemsTheSame(oldItem: MutualFundHolding, newItem: MutualFundHolding): Boolean {
            // Check if the items represent the same entity (e.g., by a unique ID)
            // Assuming fundName is unique enough for now, or add a proper 'id' to MutualFundHolding
            return oldItem.fundName == newItem.fundName
        }

        override fun areContentsTheSame(oldItem: MutualFundHolding, newItem: MutualFundHolding): Boolean {
            // Check if the data content of the items is the same
            return oldItem == newItem // Data class automatically provides equals() for content comparison
        }
    }
}