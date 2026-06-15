package com.example.budgetbuddy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val db = AppDatabase.getDatabase(this)
        
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etEmail = findViewById<EditText>(R.id.etEmailSignUp)
        val etPassword = findViewById<EditText>(R.id.etPasswordSignUp)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val cbTerms = findViewById<CheckBox>(R.id.cbTerms)
        val btnCreateAccount = findViewById<Button>(R.id.btnCreateAccount)
        val tvSignInLink = findViewById<TextView>(R.id.tvSignInLink)

        btnCreateAccount.setOnClickListener {
            val name = etUsername.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                if (password != confirmPassword) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                if (!cbTerms.isChecked) {
                    Toast.makeText(this, "Please agree to the Terms of Service", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val user = User(fullName = name, email = email, password = password)
                
                lifecycleScope.launch {
                    db.userDao().registerUser(user)
                    
                    // Automatically log in the user after registration
                    val sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
                    sharedPreferences.edit().putString("UserEmail", user.email).apply()

                    Toast.makeText(this@RegisterActivity, "Registration Successful! Welcome ${user.fullName}", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        tvSignInLink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}