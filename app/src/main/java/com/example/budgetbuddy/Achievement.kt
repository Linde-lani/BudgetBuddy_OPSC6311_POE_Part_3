package com.example.budgetbuddy

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val type: String, // e.g., "SAVINGS_GOAL", "STREAK", "CATEGORY_MASTER"
    val requirementValue: Double,
    var isUnlocked: Boolean = false,
    val iconName: String
)
