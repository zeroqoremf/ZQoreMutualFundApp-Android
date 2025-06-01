// app/src/main/java/com/zeroqore/mutualfundapp/ui/menu/MenuAdapter.kt
package com.zeroqore.mutualfundapp.ui.menu

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zeroqore.mutualfundapp.data.MenuItem // Import the MenuItem data class
import com.zeroqore.mutualfundapp.databinding.ItemMenuOptionBinding // NEW: Import for the item layout binding

class MenuAdapter(private val menuItems: List<MenuItem>) :
    RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    // Optional: Define a click listener interface
    var onMenuItemClickListener: ((MenuItem) -> Unit)? = null

    class MenuViewHolder(private val binding: ItemMenuOptionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(menuItem: MenuItem) {
            binding.menuOptionTitleTextView.text = menuItem.title
            // If you had an icon, you would set it here:
            // binding.menuIconImageView.setImageResource(menuItem.iconResId)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = ItemMenuOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menuItem = menuItems[position]
        holder.bind(menuItem)
        holder.itemView.setOnClickListener {
            onMenuItemClickListener?.invoke(menuItem)
        }
    }

    override fun getItemCount(): Int {
        return menuItems.size
    }
}