package com.zeroqore.mutualfundapp
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar // Important: Import Toolbar
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView // If you have a BottomNavigationView
import com.zeroqore.mutualfundapp.R

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Find the Toolbar from your layout and set it as the support action bar.
        // This is the direct fix for the "does not have an ActionBar set" error.
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar) // THIS LINE IS ESSENTIAL!

        // 2. Get the NavController. Its ID is from activity_main.xml.
        navController = findNavController(R.id.nav_host_fragment_activity_main)

        // 3. Set up the AppBarConfiguration with the IDs of your top-level destinations.
        // Since you have a BottomNavigationView, its menu items are typically your top-level destinations.
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        appBarConfiguration = AppBarConfiguration(navView.menu) // Use the menu from your BottomNavigationView

        // 4. Link the ActionBar/Toolbar with the NavController.
        // This handles title updates and the Up button.
        setupActionBarWithNavController(navController, appBarConfiguration)

        // 5. Link the BottomNavigationView with the NavController.
        // This handles navigation when bottom menu items are tapped.
        navView.setupWithNavController(navController)
    }

    // 6. Handle the Up button (back arrow) in the ActionBar/Toolbar.
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}