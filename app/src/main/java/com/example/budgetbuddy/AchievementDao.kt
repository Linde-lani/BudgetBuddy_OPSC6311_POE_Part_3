package com.example.budgetbuddy

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface AchievementDao {
    @Insert
    suspend fun insertAchievements(achievements: List<Achievement>)

    @Query("SELECT * FROM achievements")
    suspend fun getAllAchievements(): List<Achievement>

    @Query("SELECT * FROM achievements WHERE isUnlocked = 0")
    suspend fun getLockedAchievements(): List<Achievement>

    @Update
    suspend fun updateAchievement(achievement: Achievement)

    @Query("SELECT COUNT(*) FROM achievements")
    suspend fun getAchievementCount(): Int
}
