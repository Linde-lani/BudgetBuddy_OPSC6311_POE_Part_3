package com.example.budgetbuddy

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class AchievementsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)

        val btnBack = findViewById<TextView>(R.id.btnBackAchievements)
        btnBack.setOnClickListener {
            finish()
        }

        val rvAchievements = findViewById<RecyclerView>(R.id.rvAchievements)
        rvAchievements.layoutManager = LinearLayoutManager(this)

        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            val achievements = db.achievementDao().getAllAchievements()
            rvAchievements.adapter = AchievementAdapter(achievements)
        }
    }
}
