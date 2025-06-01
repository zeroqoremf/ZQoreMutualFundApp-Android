// app/src/main/java/com/zeroqore/mutualfundapp/ui/menu/MenuViewModel.kt
package com.zeroqore.mutualfundapp.ui.menu

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zeroqore.mutualfundapp.data.MutualFundRepository // Import the repository interface
import com.zeroqore.mutualfundapp.data.MenuItem // Import the new data class

class MenuViewModel(private val repository: MutualFundRepository) : ViewModel() {

    private val _menuItems = MutableLiveData<List<MenuItem>>()
    val menuItems: LiveData<List<MenuItem>> = _menuItems

    init {
        // Fetch menu items when the ViewModel is created
        loadMenuItems()
    }

    private fun loadMenuItems() {
        // In a real app, this would typically be an asynchronous operation (e.g., using coroutines)
        _menuItems.value = repository.getMenuItems()
    }

    // Optional: Method to refresh menu items if they were dynamic
    fun refreshMenuItems() {
        loadMenuItems()
    }
}