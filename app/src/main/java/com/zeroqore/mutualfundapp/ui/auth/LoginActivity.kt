package com.zeroqore.mutualfundapp.ui.auth

import android.content.Intent // Make sure this is imported
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.zeroqore.mutualfundapp.MainActivity
import com.zeroqore.mutualfundapp.MutualFundApplication
import com.zeroqore.mutualfundapp.databinding.ActivityLoginBinding
import com.zeroqore.mutualfundapp.ui.login.LoginViewModel
import com.zeroqore.mutualfundapp.util.Results

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val application = application as MutualFundApplication
        val loginRepository = application.container.loginRepository
        val authTokenManager = application.container.authTokenManager

        viewModel = ViewModelProvider(this, LoginViewModel.Factory(loginRepository, authTokenManager))
            .get(LoginViewModel::class.java)

        viewModel.loginResult.observe(this) { result ->
            when (result) {
                is Results.Success -> {
                    Toast.makeText(this, "Login successful: ${result.data.username ?: "User"}", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                is Results.Error -> {
                    Toast.makeText(this, "Login failed: ${result.message ?: "Unknown error"}", Toast.LENGTH_LONG).show()
                }
                is Results.Loading -> {
                    // Handled by isLoading observer, but included for completeness if needed
                }
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
                binding.loginButton.isEnabled = false
                binding.usernameEditText.isEnabled = false
                binding.passwordEditText.isEnabled = false
            } else {
                binding.progressBar.visibility = View.GONE
                binding.loginButton.isEnabled = true
                binding.usernameEditText.isEnabled = true
                binding.passwordEditText.isEnabled = true
            }
        }

        binding.loginButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                viewModel.login(username = username, password = password)
            } else {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
            }
        }

        // --- ADDED: Click listener for Forgot Password TextView ---
        binding.forgotPasswordTextView.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
        // --- END ADDED ---
    }
}