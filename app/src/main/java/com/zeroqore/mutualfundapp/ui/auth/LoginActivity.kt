package com.zeroqore.mutualfundapp.ui.auth // UPDATED PACKAGE!

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.zeroqore.mutualfundapp.MainActivity // IMPORT MainActivity to launch it
import com.zeroqore.mutualfundapp.databinding.ActivityLoginBinding // Will be generated after creating layout

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val usernameEditText: EditText = binding.usernameEditText
        val passwordEditText: EditText = binding.passwordEditText
        val loginButton: Button = binding.loginButton

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            // --- Basic Stubbed Authentication Logic ---
            if (username == "ashish@zeroqore.com" && password == "pass123") {
                // Successful login: Navigate to MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // Close LoginActivity so user can't go back to it
            } else {
                // Failed login: Show a toast message
                Toast.makeText(this, "Invalid credentials. Try 'user'/'pass'", Toast.LENGTH_LONG).show()
            }
        }
    }
}