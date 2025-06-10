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
import com.zeroqore.mutualfundapp.data.auth.ResetPasswordRequest
import com.zeroqore.mutualfundapp.databinding.ActivityResetPasswordBinding
import com.zeroqore.mutualfundapp.network.RetrofitClient
import com.zeroqore.mutualfundapp.util.Event
import com.zeroqore.mutualfundapp.util.Results
import com.zeroqore.mutualfundapp.ui.auth.LoginActivity // Ensure this import is present

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private val authViewModel: AuthViewModel by viewModels {
        val authService = RetrofitClient.authService

        val repository: LoginRepository = RemoteLoginRepository(authService)
        // For local dummy data testing, uncomment the line below and comment the one above:
        // val repository: LoginRepository = LocalLoginRepository()

        AuthViewModelFactory(repository)
    }

    // Class-level variables to store the token and identifier received from the Intent
    private var receivedResetToken: String? = null
    private var receivedIdentifier: String? = null // Storing identifier for potential future use or logging

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve token and identifier from the Intent extras
        receivedResetToken = intent.getStringExtra(EXTRA_RESET_TOKEN)
        receivedIdentifier = intent.getStringExtra(EXTRA_IDENTIFIER)

        // Optional: Log the received data for debugging (remove in production)
/*        if (receivedResetToken != null) {
            Toast.makeText(this, "Debug: Received Token: $receivedResetToken", Toast.LENGTH_SHORT).show()
        } else {
            // Handle case where token is missing (e.g., direct access to this activity)
            Toast.makeText(this, "Error: Missing reset token. Please restart password reset process.", Toast.LENGTH_LONG).show()
            finish() // Close activity if no token is present
            return
        }*/

        // The resetTokenInputLayout is now hidden in XML, so no need to interact with it directly.
        // We still need to update the description text to reflect the new flow
        binding.descriptionTextView.text = getString(R.string.reset_password_new_password_description) // Update description

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.resetPasswordButton.setOnClickListener {
            // Use the internally stored token, NOT from the EditText
            val token = receivedResetToken

            val newPassword = binding.newPasswordEditText.text.toString()
            val confirmPassword = binding.confirmNewPasswordEditText.text.toString()

            // Client-side validation: Check if token was received and other fields are valid
            if (token == null) {
                // This case should ideally be caught in onCreate, but as a fallback
                Toast.makeText(this, "Error: Reset token is missing. Please try again.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (newPassword.isEmpty()) {
                binding.newPasswordInputLayout.error = getString(R.string.error_password_required)
                return@setOnClickListener
            } else {
                binding.newPasswordInputLayout.error = null
            }

            if (newPassword != confirmPassword) {
                binding.confirmNewPasswordInputLayout.error = getString(R.string.error_passwords_not_match)
                return@setOnClickListener
            } else {
                binding.confirmNewPasswordInputLayout.error = null
            }

            binding.errorMessageTextView.visibility = View.GONE // Hide previous errors

            // Pass the internal token and collected password fields to the ViewModel
            authViewModel.confirmPasswordReset(ResetPasswordRequest(token, newPassword, confirmPassword))
        }
    }

    private fun observeViewModel() {
        authViewModel.isLoading.observe(this) { isLoading ->
            binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.resetPasswordButton.isEnabled = !isLoading
            binding.newPasswordEditText.isEnabled = !isLoading
            binding.confirmNewPasswordEditText.isEnabled = !isLoading
        }

        authViewModel.resetPasswordResult.observe(this) { event ->
            event.getContentIfNotHandled()?.let { result ->
                when (result) {
                    is Results.Success -> {
                        binding.errorMessageTextView.visibility = View.GONE
                        Toast.makeText(this, R.string.success_password_reset, Toast.LENGTH_LONG).show()

                        val intent = Intent(this, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                    }
                    is Results.Error -> {
                        val errorMessage = result.message ?: result.exception.message ?: getString(R.string.error_reset_failed)
                        binding.errorMessageTextView.text = errorMessage
                        binding.errorMessageTextView.visibility = View.VISIBLE
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    }
                    Results.Loading -> {
                        // Loading state is handled by isLoading LiveData
                    }
                }
            }
        }
    }
}