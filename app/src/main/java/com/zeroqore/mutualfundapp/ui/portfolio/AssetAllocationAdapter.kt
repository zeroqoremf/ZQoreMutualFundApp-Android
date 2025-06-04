// app/src/main/java/com/zeroqore/mutualfundapp/ui/portfolio/AssetAllocationAdapter.kt
package com.zeroqore.mutualfundapp.ui.portfolio

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zeroqore.mutualfundapp.R
import com.zeroqore.mutualfundapp.data.PortfolioDisplayItem
import com.zeroqore.mutualfundapp.databinding.ItemAssetTypeHeaderBinding
import com.zeroqore.mutualfundapp.databinding.ItemFundHoldingBinding
import java.text.NumberFormat
import java.util.Locale

class AssetAllocationAdapter(private val onHeaderClick: (String) -> Unit) :
    ListAdapter<PortfolioDisplayItem, RecyclerView.ViewHolder>(PortfolioDisplayItemDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_FUND_HOLDING = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is PortfolioDisplayItem.AssetTypeHeader -> VIEW_TYPE_HEADER
            is PortfolioDisplayItem.FundHoldingItem -> VIEW_TYPE_FUND_HOLDING
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = ItemAssetTypeHeaderBinding.inflate(inflater, parent, false)
                HeaderViewHolder(binding, onHeaderClick)
            }
            VIEW_TYPE_FUND_HOLDING -> {
                val binding = ItemFundHoldingBinding.inflate(inflater, parent, false)
                FundHoldingViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            VIEW_TYPE_HEADER -> {
                val header = getItem(position) as PortfolioDisplayItem.AssetTypeHeader
                (holder as HeaderViewHolder).bind(header)
            }
            VIEW_TYPE_FUND_HOLDING -> {
                val fundHolding = getItem(position) as PortfolioDisplayItem.FundHoldingItem
                (holder as FundHoldingViewHolder).bind(fundHolding)
            }
        }
    }

    //region ViewHolders
    inner class HeaderViewHolder(
        private val binding: ItemAssetTypeHeaderBinding,
        private val onHeaderClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val header = getItem(bindingAdapterPosition) as? PortfolioDisplayItem.AssetTypeHeader
                header?.let { onHeaderClick(it.fundType) }
            }
        }

        fun bind(header: PortfolioDisplayItem.AssetTypeHeader) {
            val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
            val percentFormatter = NumberFormat.getPercentInstance(Locale.getDefault())
            percentFormatter.minimumFractionDigits = 2
            percentFormatter.maximumFractionDigits = 2

            binding.assetTypeTextView.text = header.fundType
            binding.typeCurrentValueTextView.text = currencyFormatter.format(header.totalCurrentValue)

            val gainLossText = String.format(
                Locale.getDefault(),
                "%s%s (%s)",
                if (header.gainLossAbsolute >= 0) "+" else "",
                currencyFormatter.format(header.gainLossAbsolute),
                percentFormatter.format(header.gainLossPercentage / 100.0) // Convert percentage to decimal for formatter
            )
            binding.typeGainLossTextView.text = gainLossText
            binding.typeGainLossTextView.setTextColor(
                if (header.gainLossAbsolute >= 0) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
            )

            // Set the appropriate arrow icon
            binding.expandCollapseIcon.setImageResource(
                if (header.isExpanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down
            )
        }
    }

    inner class FundHoldingViewHolder(private val binding: ItemFundHoldingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(fundHolding: PortfolioDisplayItem.FundHoldingItem) {
            val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
            val percentFormatter = NumberFormat.getPercentInstance(Locale.getDefault())
            percentFormatter.minimumFractionDigits = 2
            percentFormatter.maximumFractionDigits = 2

            binding.fundNameTextView.text = fundHolding.holding.fundName
            binding.fundCurrentValueTextView.text = currencyFormatter.format(fundHolding.holding.currentValue)

            val gainLossText = String.format(
                Locale.getDefault(),
                "%s%s (%s)",
                if (fundHolding.absoluteReturn >= 0) "+" else "",
                currencyFormatter.format(fundHolding.absoluteReturn),
                percentFormatter.format(fundHolding.percentageReturn / 100.0) // Convert percentage to decimal
            )
            binding.fundGainLossTextView.text = gainLossText
            binding.fundGainLossTextView.setTextColor(
                if (fundHolding.absoluteReturn >= 0) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
            )
            // You might want to add a click listener here to navigate to FundDetailFragment
            // binding.root.setOnClickListener { /* navigate to fund detail */ }
        }
    }
    //endregion

    // DiffUtil for efficient list updates
    class PortfolioDisplayItemDiffCallback : DiffUtil.ItemCallback<PortfolioDisplayItem>() {
        override fun areItemsTheSame(oldItem: PortfolioDisplayItem, newItem: PortfolioDisplayItem): Boolean {
            return when {
                oldItem is PortfolioDisplayItem.AssetTypeHeader && newItem is PortfolioDisplayItem.AssetTypeHeader ->
                    oldItem.uniqueId == newItem.uniqueId
                oldItem is PortfolioDisplayItem.FundHoldingItem && newItem is PortfolioDisplayItem.FundHoldingItem ->
                    oldItem.uniqueId == newItem.uniqueId
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: PortfolioDisplayItem, newItem: PortfolioDisplayItem): Boolean {
            return oldItem == newItem // Data classes automatically generate equals() based on content
        }
    }
}