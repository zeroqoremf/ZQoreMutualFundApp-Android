// app/src/main/java/com/zeroqore/mutualfundapp/ui/auth/LoginActivity.kt
package com.zeroqore.mutualfundapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.zeroqore.mutualfundapp.MainActivity
import com.zeroqore.mutualfundapp.MutualFundApplication
import com.zeroqore.mutualfundapp.databinding.ActivityLoginBinding
import com.zeroqore.mutualfundapp.ui.login.LoginViewModel
import com.zeroqore.mutualfundapp.util.Results // Import your custom Results sealed class

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the application instance to access AppContainer
        val application = application as MutualFundApplication
        val loginRepository = application.container.loginRepository
        val authTokenManager = application.container.authTokenManager // GET AUTH TOKEN MANAGER

        // Initialize ViewModel using the custom Factory
        viewModel = ViewModelProvider(this, LoginViewModel.Factory(loginRepository, authTokenManager))
            .get(LoginViewModel::class.java)

        // CORRECTED: Observe loginResult LiveData
        viewModel.loginResult.observe(this) { result ->
            when (result) {
                is Results.Success -> {
                    // CORRECTED: Access 'username' from result.data (LoginResponse)
                    Toast.makeText(this, "Login successful: ${result.data.username ?: "User"}", Toast.LENGTH_LONG).show()
                    // Navigate to MainActivity on successful login
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish() // Close LoginActivity
                }
                is Results.Error -> {
                    Toast.makeText(this, "Login failed: ${result.message ?: "Unknown error"}", Toast.LENGTH_LONG).show()
                }
                is Results.Loading -> {
                    // Handled by isLoading observer, but included for completeness if needed
                }
            }
        }

        // CORRECTED: Observe isLoading LiveData for UI state
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE // Use binding for ProgressBar
                binding.loginButton.isEnabled = false
                binding.usernameEditText.isEnabled = false
                binding.passwordEditText.isEnabled = false
            } else {
                binding.progressBar.visibility = View.GONE // Use binding for ProgressBar
                binding.loginButton.isEnabled = true
                binding.usernameEditText.isEnabled = true
                binding.passwordEditText.isEnabled = true
            }
        }

        // Set up login button click listener
        binding.loginButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                // MODIFIED: Call login with username and password directly
                viewModel.login(username = username, password = password)
            } else {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}