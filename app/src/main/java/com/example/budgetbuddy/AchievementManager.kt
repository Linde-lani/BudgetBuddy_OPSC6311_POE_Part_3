package com.example.budgetbuddy

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AchievementManager(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val sharedPrefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)

    suspend fun initializeAchievements() {
        if (db.achievementDao().getAchievementCount() == 0) {
            val initialAchievements = listOf(
                Achievement(title = "Thrifty Saver", description = "Saved your first R1000", type = "TOTAL_SAVINGS", requirementValue = 1000.0, iconName = "ic_savings"),
                Achievement(title = "Wealth Builder", description = "Reached a total balance of R5000", type = "TOTAL_BALANCE", requirementValue = 5000.0, iconName = "ic_wealth"),
                Achievement(title = "Budget Master", description = "Stayed under budget for a month", type = "BUDGET_MASTER", requirementValue = 1.0, iconName = "ic_master"),
                Achievement(title = "Early Bird", description = "Added your first income", type = "FIRST_INCOME", requirementValue = 1.0, iconName = "ic_income"),
                Achievement(title = "Consistency King", description = "Logged transactions for 3 days in a row", type = "STREAK", requirementValue = 3.0, iconName = "ic_streak")
            )
            db.achievementDao().insertAchievements(initialAchievements)
        }
    }

    fun updateStreak() {
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val lastDate = sharedPrefs.getString("LastActivityDate", "")
        var currentStreak = sharedPrefs.getInt("CurrentStreak", 0)

        if (lastDate == today) return // Already updated today

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, -1)
        val yesterday = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(calendar.time)

        if (lastDate == yesterday) {
            currentStreak++
        } else {
            currentStreak = 1
        }

        sharedPrefs.edit().apply {
            putString("LastActivityDate", today)
            putInt("CurrentStreak", currentStreak)
            apply()
        }
    }

    suspend fun checkAchievements() {
        val lockedAchievements = db.achievementDao().getLockedAchievements()
        if (lockedAchievements.isEmpty()) return

        val totalIncome = db.transactionDao().getTotalIncome() ?: 0.0
        val totalExpense = db.transactionDao().getTotalExpense() ?: 0.0
        val totalBalance = totalIncome - totalExpense
        val currentStreak = sharedPrefs.getInt("CurrentStreak", 0)

        for (achievement in lockedAchievements) {
            var shouldUnlock = false
            when (achievement.type) {
                "TOTAL_SAVINGS" -> if (totalBalance >= achievement.requirementValue) shouldUnlock = true
                "TOTAL_BALANCE" -> if (totalBalance >= achievement.requirementValue) shouldUnlock = true
                "FIRST_INCOME" -> if (totalIncome > 0) shouldUnlock = true
                "STREAK" -> if (currentStreak >= achievement.requirementValue.toInt()) shouldUnlock = true
            }

            if (shouldUnlock) {
                achievement.isUnlocked = true
                db.achievementDao().updateAchievement(achievement)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "🏆 Achievement Unlocked: ${achievement.title}!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
