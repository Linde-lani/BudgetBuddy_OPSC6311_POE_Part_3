package com.example.budgetbuddy

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val db = AppDatabase.getDatabase(this)
        val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("DarkMode", false)

        val btnBack = findViewById<TextView>(R.id.btnBack)
        btnBack.setOnClickListener {
            onBackPressed()
        }

        val badgeContainer = findViewById<LinearLayout>(R.id.badgeContainer)
        lifecycleScope.launch {
            val unlockedAchievements = db.achievementDao().getAllAchievements().filter { it.isUnlocked }
            if (unlockedAchievements.isNotEmpty()) {
                badgeContainer.removeAllViews()
                for (achievement in unlockedAchievements) {
                    val badgeIcon = ImageView(this@SettingsActivity)
                    val size = (40 * resources.displayMetrics.density).toInt()
                    val params = LinearLayout.LayoutParams(size, size)
                    params.setMargins(0, 0, 16, 0)
                    badgeIcon.layoutParams = params
                    badgeIcon.setImageResource(android.R.drawable.btn_star_big_on)
                    badgeIcon.setColorFilter(getColor(R.color.logo_yellow))
                    badgeIcon.setOnClickListener {
                        Toast.makeText(this@SettingsActivity, achievement.title, Toast.LENGTH_SHORT).show()
                    }
                    badgeContainer.addView(badgeIcon)
                }
            }
        }

        val btnSaveProfile = findViewById<TextView>(R.id.btnSaveProfile)
        val etMonthlyBudget = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etMonthlyBudget)
        val btnUpdateBudget = findViewById<android.widget.Button>(R.id.btnUpdateBudget)
        val tilMonthlyBudget = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilMonthlyBudget)

        etMonthlyBudget.setText(sharedPreferences.getString("MonthlyBudget", "0.00"))

        val saveBudgetLogic = {
            val budget = etMonthlyBudget.text.toString()
            sharedPreferences.edit().putString("MonthlyBudget", budget).apply()
            Toast.makeText(this, "Monthly Budget updated!", Toast.LENGTH_SHORT).show()
        }

        btnUpdateBudget.setOnClickListener { saveBudgetLogic() }
        tilMonthlyBudget.setEndIconOnClickListener { saveBudgetLogic() }

        btnSaveProfile.setOnClickListener {
            val budget = etMonthlyBudget.text.toString()
            sharedPreferences.edit().putString("MonthlyBudget", budget).apply()
            Toast.makeText(this, "Profile changes saved!", Toast.LENGTH_SHORT).show()
        }

        val btnDeleteAccount = findViewById<TextView>(R.id.btnDeleteAccount)
        btnDeleteAccount.setOnClickListener {
            Toast.makeText(this, "Account deletion requested", Toast.LENGTH_SHORT).show()
        }

        val btnLogout = findViewById<android.widget.Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            sharedPreferences.edit().remove("UserEmail").apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        
        val tvChangePassword = findViewById<TextView>(R.id.tvChangePassword)
        tvChangePassword.setOnClickListener {
            val intent = Intent(this, ResetPasswordActivity::class.java)
            startActivity(intent)
        }

        val ivProfilePic = findViewById<com.google.android.material.imageview.ShapeableImageView>(R.id.ivProfilePic)
        ivProfilePic.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        val switchDarkMode = findViewById<SwitchMaterial>(R.id.switchDarkMode)
        switchDarkMode.isChecked = isDarkMode

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                sharedPreferences.edit().putBoolean("DarkMode", true).apply()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                sharedPreferences.edit().putBoolean("DarkMode", false).apply()
            }
        }

        // Navigation
        findViewById<TextView>(R.id.budgetButton).setOnClickListener {
            startActivity(Intent(this, BudgetActivity::class.java))
        }
        findViewById<TextView>(R.id.dashboardButton).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Highlight current page
        val settingsButtonView = findViewById<TextView>(R.id.settingsButton)
        settingsButtonView.setBackgroundColor(getColor(R.color.nav_active))
        settingsButtonView.scaleX = 1.1f
        settingsButtonView.scaleY = 1.1f
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            val imageUri = data?.data
            val ivProfilePic = findViewById<com.google.android.material.imageview.ShapeableImageView>(R.id.ivProfilePic)
            ivProfilePic.setImageURI(imageUri)
            Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show()
        }
    }
}
