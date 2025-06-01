// app/src/main/java/com/zeroqore/mutualfundapp/MainActivity.kt
package com.zeroqore.mutualfundapp

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.zeroqore.mutualfundapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration // Make it a class property

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the custom Toolbar as the ActionBar
        setSupportActionBar(binding.toolbar) // Ensure this line is present from previous step

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Initialize appBarConfiguration here
        appBarConfiguration = AppBarConfiguration( // Initialize the class property
            setOf(
                R.id.dashboardFragment,
                R.id.navigation_portfolio,
                R.id.navigation_transactions,
                R.id.navigation_menu
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    // ADD THIS METHOD
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // This will attempt to navigate up the hierarchy or pop the back stack
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}