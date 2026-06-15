package com.example.budgetbuddy

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TransactionDao {
    @Insert
    suspend fun addTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE type = :type")
    suspend fun getTransactionsByType(type: String): List<Transaction>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE'")
    suspend fun getTotalExpense(): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME'")
    suspend fun getTotalIncome(): Double?

    @Query("SELECT MAX(amount) FROM transactions WHERE type = 'EXPENSE'")
    suspend fun getMaxExpense(): Double?

    @Query("SELECT MIN(amount) FROM transactions WHERE type = 'EXPENSE'")
    suspend fun getMinExpense(): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME' AND timestamp >= :startTime")
    suspend fun getMonthlyIncome(startTime: Long): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND timestamp >= :startTime")
    suspend fun getMonthlyExpense(startTime: Long): Double?

    @Query("SELECT category as categoryName, SUM(amount) as totalAmount FROM transactions WHERE type = 'EXPENSE' AND timestamp >= :startTime GROUP BY category")
    suspend fun getExpenseByCategory(startTime: Long): List<CategorySummary>

    @Insert
    suspend fun addCategory(category: Category)

    @Query("SELECT * FROM categories")
    suspend fun getAllCategories(): List<Category>

    @Query("SELECT strftime('%Y-%m-%d', timestamp / 1000, 'unixepoch') as date, SUM(amount) as total FROM transactions WHERE type = 'EXPENSE' AND timestamp >= :startTime GROUP BY date ORDER BY date ASC")
    suspend fun getDailySpending(startTime: Long): List<DailySpending>
}

data class DailySpending(
    val date: String,
    val total: Double
)

data class CategorySummary(
    val categoryName: String,
    val totalAmount: Double
)