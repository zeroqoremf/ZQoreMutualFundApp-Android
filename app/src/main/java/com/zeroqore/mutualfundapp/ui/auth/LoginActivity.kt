package com.zeroqore.mutualfundapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View // Import View for visibility
import android.widget.ProgressBar // Import ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider // Import ViewModelProvider
import com.zeroqore.mutualfundapp.MainActivity
import com.zeroqore.mutualfundapp.MutualFundApplication // Import your Application class
import com.zeroqore.mutualfundapp.databinding.ActivityLoginBinding
import com.zeroqore.mutualfundapp.data.auth.LoginRequest // Import LoginRequest

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel // Declare ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the application instance to access AppContainer
        val application = application as MutualFundApplication
        val loginRepository = application.container.loginRepository

        // Initialize ViewModel using the custom Factory
        viewModel = ViewModelProvider(this, LoginViewModel.Factory(loginRepository))
            .get(LoginViewModel::class.java)

        // Removed explicit local val declarations, using binding directly
        val progressBar: ProgressBar? = findViewById(com.zeroqore.mutualfundapp.R.id.progressBar) // Still requires findViewById if not in binding

        // Observe the login UI state from the ViewModel
        viewModel.loginUiState.observe(this) { state ->
            when (state) {
                is LoginUiState.Idle -> {
                    progressBar?.visibility = View.GONE
                    binding.loginButton.isEnabled = true // Direct use of binding
                    binding.usernameEditText.isEnabled = true // Direct use of binding
                    binding.passwordEditText.isEnabled = true // Direct use of binding
                }
                is LoginUiState.Loading -> {
                    progressBar?.visibility = View.VISIBLE
                    binding.loginButton.isEnabled = false // Direct use of binding
                    binding.usernameEditText.isEnabled = false // Direct use of binding
                    binding.passwordEditText.isEnabled = false // Direct use of binding
                }
                is LoginUiState.Success -> {
                    progressBar?.visibility = View.GONE
                    binding.loginButton.isEnabled = true // Direct use of binding
                    binding.usernameEditText.isEnabled = true // Direct use of binding
                    binding.passwordEditText.isEnabled = true // Direct use of binding
                    Toast.makeText(this, "Login successful: ${state.response.investorName}", Toast.LENGTH_LONG).show()
                    // Navigate to MainActivity on successful login
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish() // Close LoginActivity
                }
                is LoginUiState.Error -> {
                    progressBar?.visibility = View.GONE
                    binding.loginButton.isEnabled = true // Direct use of binding
                    binding.usernameEditText.isEnabled = true // Direct use of binding
                    binding.passwordEditText.isEnabled = true // Direct use of binding
                    Toast.makeText(this, "Login failed: ${state.message}", Toast.LENGTH_LONG).show()
                    viewModel.resetUiState() // Reset ViewModel state to allow re-login attempts
                }
            }
        }

        // Set up login button click listener
        binding.loginButton.setOnClickListener { // Direct use of binding
            val username = binding.usernameEditText.text.toString() // Direct use of binding
            val password = binding.passwordEditText.text.toString() // Direct use of binding

            if (username.isNotEmpty() && password.isNotEmpty()) {
                // Call the login function on the ViewModel
                viewModel.login(LoginRequest(username = username, password = password))
            } else {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}