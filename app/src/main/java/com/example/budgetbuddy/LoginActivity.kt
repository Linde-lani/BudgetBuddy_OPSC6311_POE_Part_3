package com.example.budgetbuddy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val db = AppDatabase.getDatabase(this)

        val etEmail = findViewById<EditText>(R.id.Email)
        val etPassword = findViewById<EditText>(R.id.Password)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                lifecycleScope.launch {
                    val user = db.userDao().loginUser(email, password)
                    if (user != null) {
                        // Save user email to SharedPreferences
                        val sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
                        sharedPreferences.edit().putString("UserEmail", user.email).apply()

                        Toast.makeText(this@LoginActivity, "Login Successful! Welcome ${user.fullName}", Toast.LENGTH_SHORT).show()
                        
                        // Link to the MainActivity (Dashboard) after successful login
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish() // Close login screen so user can't go back to it
                    } else {
                        Toast.makeText(this@LoginActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        tvSignUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}