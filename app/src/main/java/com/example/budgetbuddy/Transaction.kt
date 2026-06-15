package com.example.budgetbuddy

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val reason: String,
    val type: String, // "EXPENSE" or "INCOME"
    val category: String = "Other",
    val timestamp: Long = System.currentTimeMillis(),
    val imagePath: String? = null
)