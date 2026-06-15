package com.example.budgetbuddy

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class BudgetActivity : AppCompatActivity() {

    private lateinit var totalExpense: TextView
    private lateinit var totalIncome: TextView
    private lateinit var tvMonthlyBudget: TextView

    private lateinit var addExpense: TextView
    private lateinit var addIncome: TextView
    private lateinit var expenseShow: TextView
    private lateinit var incomeShow: TextView
    private lateinit var adjustBudget: TextView

    private lateinit var db: AppDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_budget)

        db = AppDatabase.getDatabase(this)
        totalExpense = findViewById(R.id.totalExpense)
        totalIncome = findViewById(R.id.totalIncome)
        tvMonthlyBudget = findViewById(R.id.tvMonthlyBudget)

        addExpense = findViewById(R.id.addExpense)
        addIncome = findViewById(R.id.addIncome)
        expenseShow = findViewById(R.id.expenseShow)
        incomeShow = findViewById(R.id.incomeShow)
        adjustBudget = findViewById(R.id.adjustBudget)

        adjustBudget.setOnClickListener {
            showAdjustBudgetDialog()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }
        val budgetButton = findViewById<TextView>(R.id.budgetButton)
        budgetButton.setOnClickListener {
            val intent = Intent(this, BudgetActivity::class.java)
            startActivity(intent)
        }
        val dashboardButton = findViewById<TextView>(R.id.dashboardButton)
        dashboardButton.setOnClickListener {
            val intent= Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        findViewById<TextView>(R.id.settingsButton).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Highlight current page
        budgetButton.setBackgroundColor(getColor(R.color.nav_active))
        budgetButton.scaleX = 1.1f
        budgetButton.scaleY = 1.1f

        addExpense.setOnClickListener {
            AddActivity.IS_EXPENSE = true
            startActivity(Intent(this, AddActivity::class.java))
        }

        addIncome.setOnClickListener {
            AddActivity.IS_EXPENSE = false
            startActivity(Intent(this, AddActivity::class.java))
        }

        expenseShow.setOnClickListener {
            RecyclerViewActivity.REC_VIEW_TYPE = "EXPENSE"
            startActivity(Intent(this, RecyclerViewActivity::class.java))
        }

        incomeShow.setOnClickListener {
            RecyclerViewActivity.REC_VIEW_TYPE = "INCOME"
            startActivity(Intent(this, RecyclerViewActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        updateDashboard()
    }

    private fun updateDashboard() {
        lifecycleScope.launch {
            val income = db.transactionDao().getTotalIncome() ?: 0.0
            val expense = db.transactionDao().getTotalExpense() ?: 0.0

            totalIncome.text = "R %.2f".format(income)
            totalExpense.text = "R %.2f".format(expense)

            val sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
            val monthlyBudgetVal = sharedPreferences.getString("MonthlyBudget", "0.00")?.toDoubleOrNull() ?: 0.0
            tvMonthlyBudget.text = "R %.2f".format(monthlyBudgetVal)
        }
    }

    private fun showAdjustBudgetDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Adjust Monthly Budget")

        val input = android.widget.EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        val sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        input.setText(sharedPreferences.getString("MonthlyBudget", "0.00"))
        builder.setView(input)

        builder.setPositiveButton("Save") { _, _ ->
            val newBudget = input.text.toString()
            sharedPreferences.edit().putString("MonthlyBudget", newBudget).apply()
            updateDashboard()
            android.widget.Toast.makeText(this, "Budget Updated", android.widget.Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }
}
