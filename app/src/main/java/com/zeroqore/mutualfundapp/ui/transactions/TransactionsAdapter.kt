// app/src/main/java/com/zeroqore/mutualfundapp/ui/transactions/TransactionsAdapter.kt
package com.zeroqore.mutualfundapp.ui.transactions

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zeroqore.mutualfundapp.data.MutualFundTransaction // Import the transaction data class
import com.zeroqore.mutualfundapp.databinding.ItemTransactionBinding // NEW: Import for the item layout binding
import java.text.NumberFormat
import java.util.Locale

class TransactionsAdapter(private val transactions: List<MutualFundTransaction>) :
    RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(private val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root) {
        private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())

        fun bind(transaction: MutualFundTransaction) {
            binding.transactionDateTextView.text = transaction.transactionDate
            binding.fundNameTextView.text = transaction.fundName
            binding.transactionUnitsTextView.text =
                if (transaction.units > 0) "Units: ${"%.2f".format(transaction.units)}" else "" // Only show units if > 0

            binding.transactionAmountTextView.text = currencyFormatter.format(transaction.amount)

            // Set transaction type text and color/background
            binding.transactionTypeTextView.text = transaction.transactionType
            when (transaction.transactionType.uppercase(Locale.ROOT)) {
                "BUY" -> {
                    binding.transactionTypeTextView.setBackgroundResource(com.zeroqore.mutualfundapp.R.drawable.bg_transaction_type_buy)
                    binding.transactionTypeTextView.setTextColor(Color.WHITE)
                }
                "SELL" -> {
                    // For SELL, we'll need a bg_transaction_type_sell drawable (red)
                    // For now, let's just make it red if we haven't created the drawable yet
                    binding.transactionTypeTextView.setBackgroundColor(Color.parseColor("#F44336")) // Red
                    binding.transactionTypeTextView.setTextColor(Color.WHITE)
                }
                "DIVIDEND" -> {
                    binding.transactionTypeTextView.setBackgroundColor(Color.parseColor("#FFC107")) // Amber/Orange
                    binding.transactionTypeTextView.setTextColor(Color.BLACK)
                }
                else -> {
                    // Default color for other types
                    binding.transactionTypeTextView.setBackgroundColor(Color.LTGRAY)
                    binding.transactionTypeTextView.setTextColor(Color.BLACK)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.bind(transaction)
    }

    override fun getItemCount(): Int {
        return transactions.size
    }
}