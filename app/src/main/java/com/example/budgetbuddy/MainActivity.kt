package com.example.budgetbuddy

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var mainBalance: TextView
    private lateinit var tvStreak: TextView
    private lateinit var tvMonthlySavings: TextView
    private lateinit var tvMonthlyBudget: TextView
    private lateinit var tvTotalSpentMonth: TextView
    private lateinit var tvMinExpense: TextView
    private lateinit var tvMaxExpense: TextView
    private lateinit var categoryContainer: LinearLayout
    private lateinit var pieChart: PieChart
    private lateinit var lineChart: LineChart
    private lateinit var barChart: BarChart
    private lateinit var spinnerPeriod: Spinner

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = AppDatabase.getDatabase(this)
        
        val achievementManager = AchievementManager(this)
        achievementManager.updateStreak()

        mainBalance = findViewById(R.id.mainBalance)
        tvStreak = findViewById(R.id.tvStreak)
        tvMonthlySavings = findViewById(R.id.tvMonthlySavings)
        tvMonthlyBudget = findViewById(R.id.tvMonthlyBudget)
        tvTotalSpentMonth = findViewById(R.id.tvTotalSpentMonth)
        tvMinExpense = findViewById(R.id.tvMinExpense)
        tvMaxExpense = findViewById(R.id.tvMaxExpense)
        categoryContainer = findViewById(R.id.categoryContainer)
        pieChart = findViewById(R.id.pieChart)
        lineChart = findViewById(R.id.lineChart)
        barChart = findViewById(R.id.barChart)
        spinnerPeriod = findViewById(R.id.spinnerPeriod)

        setupPeriodSpinner()

        val sharedPrefs = getSharedPreferences("Settings", MODE_PRIVATE)
        val streak = sharedPrefs.getInt("CurrentStreak", 0)
        tvStreak.text = "$streak Day Streak"

        updateDashboard()

        findViewById<TextView>(R.id.budgetButton).setOnClickListener {
            startActivity(Intent(this, BudgetActivity::class.java))
        }
        findViewById<TextView>(R.id.dashboardButton).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        findViewById<TextView>(R.id.settingsButton).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.cardAchievements).setOnClickListener {
            startActivity(Intent(this, AchievementsActivity::class.java))
        }

        tvMonthlyBudget.setOnClickListener {
            showAdjustBudgetDialog()
        }

        // Highlight current page
        val dashboardButtonView = findViewById<TextView>(R.id.dashboardButton)
        dashboardButtonView.setBackgroundColor(getColor(R.color.nav_active))
        dashboardButtonView.scaleX = 1.1f
        dashboardButtonView.scaleY = 1.1f
    }

    private fun setupPeriodSpinner() {
        val periods = arrayOf("This Month", "Last 7 Days", "Last 30 Days", "All Time")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, periods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPeriod.adapter = adapter

        spinnerPeriod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateBarChart(periods[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun onResume() {
        super.onResume()
        updateDashboard()
    }

    private fun updateDashboard() {
        lifecycleScope.launch {
            val totalIncome = db.transactionDao().getTotalIncome() ?: 0.0
            val totalExpense = db.transactionDao().getTotalExpense() ?: 0.0
            val balance = totalIncome - totalExpense
            mainBalance.text = "R %.2f".format(balance)

            val minExpense = db.transactionDao().getMinExpense() ?: 0.0
            val maxExpense = db.transactionDao().getMaxExpense() ?: 0.0
            tvMinExpense.text = "R %.2f".format(minExpense)
            tvMaxExpense.text = "R %.2f".format(maxExpense)

            // Monthly Summary
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val startOfMonth = calendar.timeInMillis

            val monthlyIncome = db.transactionDao().getMonthlyIncome(startOfMonth) ?: 0.0
            val monthlyExpense = db.transactionDao().getMonthlyExpense(startOfMonth) ?: 0.0
            val monthlySavings = monthlyIncome - monthlyExpense
            
            tvTotalSpentMonth.text = "R %.2f".format(monthlyExpense)

            val sharedPrefs = getSharedPreferences("Settings", MODE_PRIVATE)
            val monthlyBudgetVal = sharedPrefs.getString("MonthlyBudget", "0.00")?.toDoubleOrNull() ?: 0.0
            tvMonthlyBudget.text = "Monthly Budget: R %.2f".format(monthlyBudgetVal)

            if (monthlySavings >= 0) {
                tvMonthlySavings.text = "Monthly Savings: R %.2f".format(monthlySavings)
                tvMonthlySavings.setTextColor(getColor(android.R.color.holo_green_dark))
            } else {
                tvMonthlySavings.text = "Monthly Deficit: R %.2f".format(Math.abs(monthlySavings))
                tvMonthlySavings.setTextColor(getColor(android.R.color.holo_red_dark))
            }

            // Category Breakdown
            val summaries = db.transactionDao().getExpenseByCategory(startOfMonth)
            categoryContainer.removeAllViews()
            
            if (summaries.isEmpty()) {
                val emptyTv = TextView(this@MainActivity)
                emptyTv.text = "No expenses recorded this month"
                emptyTv.gravity = Gravity.CENTER
                emptyTv.setPadding(0, 50, 0, 50)
                categoryContainer.addView(emptyTv)
            } else {
                for (summary in summaries) {
                    val row = LinearLayout(this@MainActivity)
                    row.orientation = LinearLayout.HORIZONTAL
                    row.setPadding(0, 8, 0, 8)
                    
                    val nameTv = TextView(this@MainActivity)
                    nameTv.text = summary.categoryName
                    nameTv.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    nameTv.setTextColor(getColor(R.color.black))
                    
                    val amountTv = TextView(this@MainActivity)
                    amountTv.text = "R %.2f".format(summary.totalAmount)
                    amountTv.setTextColor(getColor(R.color.black))
                    amountTv.setTypeface(null, android.graphics.Typeface.BOLD)
                    
                    row.addView(nameTv)
                    row.addView(amountTv)
                    categoryContainer.addView(row)
                }
            }

            // Update Achievement Count
            val achievements = db.achievementDao().getAllAchievements()
            val unlockedCount = achievements.count { it.isUnlocked }
            findViewById<TextView>(R.id.tvAchievementCount).text = "$unlockedCount/${achievements.size}"

            // Setup Charts
            setupPieChart(monthlyIncome, monthlyExpense)
            setupLineChart(startOfMonth)
            updateBarChart(spinnerPeriod.selectedItem.toString())
        }
    }

    private fun setupPieChart(income: Double, expense: Double) {
        val entries = ArrayList<PieEntry>()
        if (income > 0 || expense > 0) {
            if (income > 0) entries.add(PieEntry(income.toFloat(), "Income"))
            if (expense > 0) entries.add(PieEntry(expense.toFloat(), "Expenses"))
        } else {
            entries.add(PieEntry(1f, "No Data"))
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(
            getColor(android.R.color.holo_green_light),
            getColor(android.R.color.holo_red_light)
        )
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.centerText = "Monthly Ratio"
        pieChart.animateY(1000)
        pieChart.invalidate()
    }

    private fun setupLineChart(startTime: Long) {
        lifecycleScope.launch {
            val dailySpending = db.transactionDao().getDailySpending(startTime)
            val entries = ArrayList<Entry>()
            val labels = ArrayList<String>()

            if (dailySpending.isEmpty()) {
                entries.add(Entry(0f, 0f))
                labels.add("N/A")
            } else {
                dailySpending.forEachIndexed { index, spending ->
                    entries.add(Entry(index.toFloat(), spending.total.toFloat()))
                    labels.add(spending.date.substring(8)) // Day only
                }
            }

            val dataSet = LineDataSet(entries, "Daily Spending")
            dataSet.color = getColor(R.color.primary)
            dataSet.setCircleColor(getColor(R.color.primary))
            dataSet.lineWidth = 2f
            dataSet.circleRadius = 4f
            dataSet.setDrawCircleHole(false)
            dataSet.valueTextSize = 10f
            dataSet.setDrawFilled(true)
            dataSet.fillColor = getColor(R.color.soft_yellow)

            val lineData = LineData(dataSet)
            lineChart.data = lineData
            
            val xAxis = lineChart.xAxis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.granularity = 1f
            
            lineChart.axisRight.isEnabled = false
            lineChart.description.isEnabled = false
            lineChart.animateX(1000)
            lineChart.invalidate()
        }
    }

    private fun updateBarChart(period: String) {
        val calendar = Calendar.getInstance()
        val startTime = when (period) {
            "This Month" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.timeInMillis
            }
            "Last 7 Days" -> {
                calendar.add(Calendar.DATE, -7)
                calendar.timeInMillis
            }
            "Last 30 Days" -> {
                calendar.add(Calendar.DATE, -30)
                calendar.timeInMillis
            }
            else -> 0L // All Time
        }

        lifecycleScope.launch {
            val summaries = db.transactionDao().getExpenseByCategory(startTime)
            val entries = ArrayList<BarEntry>()
            val labels = ArrayList<String>()

            summaries.forEachIndexed { index, summary ->
                entries.add(BarEntry(index.toFloat(), summary.totalAmount.toFloat()))
                labels.add(summary.categoryName)
            }

            val dataSet = BarDataSet(entries, "Spending by Category")
            dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
            dataSet.valueTextColor = Color.BLACK
            dataSet.valueTextSize = 10f

            val barData = BarData(dataSet)
            barChart.data = barData

            val xAxis = barChart.xAxis
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)

            // Min/Max Goals (Limit Lines)
            val leftAxis = barChart.axisLeft
            leftAxis.removeAllLimitLines()
            
            val maxGoal = getSharedPreferences("Settings", MODE_PRIVATE).getString("MonthlyBudget", "2000")?.toDoubleOrNull() ?: 2000.0
            val minGoal = maxGoal * 0.2 // Example: 20% of budget as a min "goal"

            val maxLimit = LimitLine(maxGoal.toFloat(), "Max Budget")
            maxLimit.lineWidth = 2f
            maxLimit.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
            maxLimit.textSize = 10f
            maxLimit.lineColor = Color.RED

            val minLimit = LimitLine(minGoal.toFloat(), "Min Goal")
            minLimit.lineWidth = 2f
            minLimit.labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM
            minLimit.textSize = 10f
            minLimit.lineColor = Color.GREEN

            leftAxis.addLimitLine(maxLimit)
            leftAxis.addLimitLine(minLimit)
            leftAxis.axisMinimum = 0f

            barChart.axisRight.isEnabled = false
            barChart.description.isEnabled = false
            barChart.animateY(1000)
            barChart.invalidate()
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
            Toast.makeText(this, "Budget Updated", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }
}
