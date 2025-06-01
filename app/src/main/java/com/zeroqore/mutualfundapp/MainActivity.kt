// app/src/main/java/com/zeroqore/mutualfundapp/MainActivity.kt
package com.zeroqore.mutualfundapp

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.zeroqore.mutualfundapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        // Correct ID for the NavHostFragment is nav_host_fragment_activity_main
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.dashboardFragment, // This ID is from your mobile_navigation.xml
                R.id.navigation_portfolio, // If you have a portfolio fragment in mobile_navigation.xml
                R.id.navigation_transactions, // If you have a transactions fragment in mobile_navigation.xml
                R.id.navigation_menu // If you have a menu fragment in mobile_navigation.xml
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }
}