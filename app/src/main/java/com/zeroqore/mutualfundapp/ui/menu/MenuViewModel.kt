// app/src/main/java/com/zeroqore/mutualfundapp/ui/menu/MenuViewModel.kt
package com.zeroqore.mutualfundapp.ui.menu

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // NEW IMPORT
import com.zeroqore.mutualfundapp.data.MutualFundAppRepository
import com.zeroqore.mutualfundapp.data.MenuItem
import kotlinx.coroutines.launch // NEW IMPORT
import android.util.Log // NEW IMPORT for logging

class MenuViewModel(private val repository: MutualFundAppRepository) : ViewModel() {

    private val _menuItems = MutableLiveData<List<MenuItem>>()
    val menuItems: LiveData<List<MenuItem>> = _menuItems

    // NEW: LiveData to observe loading state for UI feedback
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // NEW: LiveData to observe error messages for UI feedback (allows null if no error)
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadMenuItems() // Initial data fetch when ViewModel is created
    }

    private fun loadMenuItems() {
        // Launch a coroutine in the ViewModel's scope.
        // This ensures the asynchronous operation is tied to the ViewModel's lifecycle.
        viewModelScope.launch {
            _isLoading.postValue(true) // Set loading state to true (on background thread)
            _errorMessage.postValue(null) // Clear any previous error message

            try {
                // Call the suspend function to fetch menu items
                val fetchedMenuItems = repository.getMenuItems()
                _menuItems.postValue(fetchedMenuItems) // Update LiveData on the main thread
                Log.d("MenuViewModel", "Fetched ${fetchedMenuItems.size} menu items successfully.")
            } catch (e: Exception) {
                // Handle any errors that occur during the data fetching
                _errorMessage.postValue("Failed to load menu items: ${e.message}")
                _menuItems.postValue(emptyList()) // Provide an empty list on error
                Log.e("MenuViewModel", "Error loading menu items", e) // Log the detailed error
            } finally {
                _isLoading.postValue(false) // Set loading state to false
            }
        }
    }

    // Method to refresh menu items, simply re-triggers the loading process
    fun refreshMenuItems() {
        loadMenuItems()
    }
}