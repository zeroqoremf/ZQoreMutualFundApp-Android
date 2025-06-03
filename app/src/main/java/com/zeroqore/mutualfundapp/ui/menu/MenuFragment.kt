// app/src/main/java/com/zeroqore/mutualfundapp/ui/menu/MenuFragment.kt
package com.zeroqore.mutualfundapp.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast // NEW IMPORT for click feedback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // NEW IMPORT
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider // NEW IMPORT
import androidx.recyclerview.widget.LinearLayoutManager // NEW IMPORT
import com.zeroqore.mutualfundapp.MutualFundApplication // NEW IMPORT
import com.zeroqore.mutualfundapp.data.MutualFundAppRepository // Import the repository interface
import com.zeroqore.mutualfundapp.databinding.FragmentMenuBinding // Make sure this import is correct

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    private lateinit var menuAdapter: MenuAdapter // Declare the adapter

    // Initialize ViewModel using ViewModelProvider.Factory
    private val menuViewModel: MenuViewModel by viewModels {
        MenuViewModelFactory((activity?.application as MutualFundApplication).container.mutualFundRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView
        binding.menuRecyclerView.layoutManager = LinearLayoutManager(context)

        // Observe menu items from the ViewModel
        menuViewModel.menuItems.observe(viewLifecycleOwner) { menuList ->
            // Initialize adapter if not already
            if (!::menuAdapter.isInitialized) {
                menuAdapter = MenuAdapter(menuList)
                binding.menuRecyclerView.adapter = menuAdapter

                // Set up click listener for menu items
                menuAdapter.onMenuItemClickListener = { menuItem ->
                    Toast.makeText(context, "Clicked: ${menuItem.title}", Toast.LENGTH_SHORT).show()
                    // Here you would add navigation logic based on menuItem.id
                    // Example: when (menuItem.id) { "profile" -> findNavController().navigate(R.id.action_menu_to_profile) }
                }
            } else {
                // If you want to update the list, you would need to add a setter in the adapter:
                // menuAdapter.updateData(menuList)
                // For now, if initialized once, this won't change as menu items are static
                binding.menuRecyclerView.adapter = MenuAdapter(menuList).apply {
                    onMenuItemClickListener = { menuItem ->
                        Toast.makeText(context, "Clicked: ${menuItem.title}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // If the list is empty, you could show a message
            binding.menuRecyclerView.visibility = if (menuList.isEmpty()) View.GONE else View.VISIBLE
            // Optionally show a TextView here if list is empty
            // binding.emptyMenuMessage.visibility = if (menuList.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// NEW: ViewModel Factory for MenuViewModel
class MenuViewModelFactory(private val repository: MutualFundAppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MenuViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MenuViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}