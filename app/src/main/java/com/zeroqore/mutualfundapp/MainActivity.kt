package com.zeroqore.mutualfundapp
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent // Import Intent for navigation
import android.util.Log // Import Log for debugging
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.zeroqore.mutualfundapp.data.AuthTokenManager // NEW: Import AuthTokenManager
import com.zeroqore.mutualfundapp.ui.auth.LoginActivity // NEW: Import LoginActivity for redirection

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var authTokenManager: AuthTokenManager // NEW: Declare AuthTokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Get AuthTokenManager instance from your application container
        val application = application as MutualFundApplication
        authTokenManager = application.container.authTokenManager

        // --- NEW: Role-based Access Control Check ---
        val userRoles = authTokenManager.getRoles()
        Log.d("MainActivity", "Current user roles: $userRoles")

        // If the user does NOT have the "ROLE_INVESTOR" role,
        // we'll log them out and redirect to the LoginActivity.
        // This ensures that only authorized INVESTORS proceed to the main app content.
        if (!userRoles.contains("ROLE_INVESTOR")) {
            Log.w("MainActivity", "Access denied: User is not an INVESTOR. Logging out.")
            authTokenManager.clearAuthData() // Clear any existing auth data
            val intent = Intent(this, LoginActivity::class.java).apply {
                // Clear the back stack to prevent user from navigating back to MainActivity
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish() // Finish MainActivity so it's removed from the back stack
            return // Stop further execution of onCreate for non-investors
        }
        // --- END NEW ---

        // Proceed with existing UI setup ONLY if the user is an INVESTOR
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        navController = findNavController(R.id.nav_host_fragment_activity_main)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        appBarConfiguration = AppBarConfiguration(navView.menu)

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    // Handle the Up button (back arrow) in the ActionBar/Toolbar.
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}