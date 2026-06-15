package com.example.budgetbuddy

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.Calendar

class AddActivity : AppCompatActivity() {

    private lateinit var buyDisplay: TextView
    private lateinit var reasonDisplay: TextView
    private lateinit var addTv: TextView
    private lateinit var edBuy: EditText
    private lateinit var edReason: EditText
    private lateinit var btnSave: Button
    private lateinit var btnSelectImage: Button
    private lateinit var spinnerCategory: Spinner

    private lateinit var db: AppDatabase
    private var selectedImagePath: String? = null

    private val categories = arrayOf("Food", "Transport", "Rent", "Entertainment", "Shopping", "Salary", "Other")

    companion object {
        var IS_EXPENSE: Boolean = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        db = AppDatabase.getDatabase(this)

        btnSave = findViewById(R.id.button)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        edBuy = findViewById(R.id.edBuy)
        edReason = findViewById(R.id.edReason)
        buyDisplay = findViewById(R.id.buyDisplay)
        reasonDisplay = findViewById(R.id.reasonDisplay)
        addTv = findViewById(R.id.addTv)
        spinnerCategory = findViewById(R.id.spinnerCategory)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        if (IS_EXPENSE) {
            addTv.text = "Add Expense"
            buyDisplay.text = "How much money did you spend?"
            reasonDisplay.text = "What is the reason for buying?"
            btnSave.text = "Add Expense"
        } else {
            addTv.text = "Add Income"
            buyDisplay.text = "How much did you earn?"
            reasonDisplay.text = "Where did you earn this money?"
            btnSave.text = "Add Income"
            spinnerCategory.setSelection(categories.indexOf("Salary"))
        }

        btnSelectImage.setOnClickListener {
            // Placeholder for image selection logic
            // In a real app, you'd use an Intent to pick an image
            Toast.makeText(this, "Image selection feature coming soon!", Toast.LENGTH_SHORT).show()
            selectedImagePath = "placeholder_path" 
        }

        btnSave.setOnClickListener {
            val amountStr = edBuy.text.toString()
            val reason = edReason.text.toString()
            val category = spinnerCategory.selectedItem.toString()

            if (amountStr.isNotEmpty() && reason.isNotEmpty()) {
                val amount = amountStr.toDoubleOrNull() ?: 0.0
                val type = if (IS_EXPENSE) "EXPENSE" else "INCOME"
                
                val transaction = Transaction(
                    amount = amount, 
                    reason = reason, 
                    type = type,
                    category = category,
                    imagePath = selectedImagePath
                )

                lifecycleScope.launch {
                    db.transactionDao().addTransaction(transaction)
                    AchievementManager(this@AddActivity).checkAchievements()
                    Toast.makeText(this@AddActivity, "The data Successfully Added", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                Toast.makeText(this, "The edit text is empty!", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<TextView>(R.id.budgetButton).setOnClickListener {
            startActivity(Intent(this, BudgetActivity::class.java))
        }
        findViewById<TextView>(R.id.dashboardButton).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        findViewById<TextView>(R.id.settingsButton).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Highlight current page (Budget is the parent for Add)
        val budgetButtonView = findViewById<TextView>(R.id.budgetButton)
        budgetButtonView.setBackgroundColor(getColor(R.color.logo_yellow))
        budgetButtonView.scaleX = 1.1f
        budgetButtonView.scaleY = 1.1f

        val btnBack = findViewById<TextView>(R.id.btnBack)
        btnBack.setOnClickListener {
            onBackPressed()
        }
    }

}