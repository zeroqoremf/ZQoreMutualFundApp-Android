package com.zeroqore.mutualfundapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.zeroqore.mutualfundapp.R
import com.zeroqore.mutualfundapp.data.auth.LoginRepository
import com.zeroqore.mutualfundapp.data.auth.LocalLoginRepository
import com.zeroqore.mutualfundapp.data.auth.RemoteLoginRepository
import com.zeroqore.mutualfundapp.databinding.ActivityForgotPasswordBinding
import com.zeroqore.mutualfundapp.network.RetrofitClient
import com.zeroqore.mutualfundapp.util.Results

// Define a constant for the Intent extra key
const val EXTRA_RESET_TOKEN = "com.zeroqore.mutualfundapp.RESET_TOKEN"
const val EXTRA_IDENTIFIER = "com.zeroqore.mutualfundapp.IDENTIFIER" // To pass username/email to reset screen

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val authViewModel: AuthViewModel by viewModels {
        val authService = RetrofitClient.authService

        // Choose which repository to use: RemoteLoginRepository for actual API, LocalLoginRepository for local testing
        // For production, you'll likely use RemoteLoginRepository
        val repository: LoginRepository = RemoteLoginRepository(authService)
        // If you want to use the local dummy data for testing the UI flow:
        // val repository: LoginRepository = LocalLoginRepository()

        AuthViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.sendResetLinkButton.setOnClickListener {
            val usernameOrEmail = binding.usernameEmailEditText.text.toString().trim()
            if (usernameOrEmail.isEmpty()) {
                binding.usernameEmailInputLayout.error = getString(R.string.error_email_required)
                binding.errorMessageTextView.visibility = View.GONE
            } else {
                binding.usernameEmailInputLayout.error = null // Clear any previous error
                authViewModel.requestPasswordReset(usernameOrEmail)
            }
        }
    }

    private fun observeViewModel() {
        authViewModel.isLoading.observe(this) { isLoading ->
            binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.sendResetLinkButton.isEnabled = !isLoading // Disable button while loading
            binding.usernameEmailEditText.isEnabled = !isLoading // Disable input while loading
        }

        authViewModel.forgotPasswordResult.observe(this) { event ->
            event.getContentIfNotHandled()?.let { result ->
                when (result) {
                    is Results.Success -> {
                        binding.errorMessageTextView.visibility = View.GONE

                        // Extract the response data
                        val responseData = result.data // This is ForgotPasswordInitiateResponse

                        // Crucial: Check if resetToken is not null
                        if (responseData.resetToken != null) {
                            Toast.makeText(this, responseData.message, Toast.LENGTH_LONG).show() // Show backend message

                            // --- NAVIGATE TO RESET PASSWORD ACTIVITY AND PASS TOKEN ---
                            val intent = Intent(this, ResetPasswordActivity::class.java).apply {
                                putExtra(EXTRA_RESET_TOKEN, responseData.resetToken)
                                putExtra(EXTRA_IDENTIFIER, responseData.identifier) // Pass identifier too for context
                            }
                            startActivity(intent)
                            finish() // Optionally, finish this activity so user can't go back here easily
                        } else {
                            // This case should ideally align with backend's secure response for unknown users.
                            // If backend sends a generic message for unknown users, display that.
                            // If backend sends 'null' token for known users but email sending failed,
                            // you might need a different message or error handling here.
                            val errorMessage = responseData.message ?: getString(R.string.error_request_failed_no_token)
                            binding.errorMessageTextView.text = errorMessage
                            binding.errorMessageTextView.visibility = View.VISIBLE
                            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                        }
                    }
                    is Results.Error -> {
                        val errorMessage = result.message ?: result.exception.message ?: getString(R.string.error_request_failed)
                        binding.errorMessageTextView.text = errorMessage
                        binding.errorMessageTextView.visibility = View.VISIBLE
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    }
                    Results.Loading -> {
                        // This state is handled by authViewModel.isLoading LiveData
                    }
                }
            }
        }
    }
}