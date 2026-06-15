package com.example.budgetbuddy

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class ResetPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        val db = AppDatabase.getDatabase(this)
        val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val userEmail = sharedPreferences.getString("UserEmail", null)

        val btnBack = findViewById<TextView>(R.id.btnBackReset)
        val etCurrentPassword = findViewById<TextInputEditText>(R.id.etCurrentPassword)
        val etNewPassword = findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmNewPassword = findViewById<TextInputEditText>(R.id.etConfirmNewPassword)
        val btnSave = findViewById<Button>(R.id.btnSaveNewPassword)

        btnBack.setOnClickListener {
            finish()
        }

        btnSave.setOnClickListener {
            val currentPass = etCurrentPassword.text.toString()
            val newPass = etNewPassword.text.toString()
            val confirmPass = etConfirmNewPassword.text.toString()

            if (userEmail == null) {
                Toast.makeText(this, "User session not found. Please login again.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass != confirmPass) {
                Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val user = db.userDao().getUserByEmail(userEmail)
                if (user != null && user.password == currentPass) {
                    db.userDao().updatePassword(userEmail, newPass)
                    Toast.makeText(this@ResetPasswordActivity, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@ResetPasswordActivity, "Incorrect current password", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
