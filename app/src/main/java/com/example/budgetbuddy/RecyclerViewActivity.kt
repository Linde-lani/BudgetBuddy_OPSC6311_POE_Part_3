package com.example.budgetbuddy

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class RecyclerViewActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var recyTv: TextView
    private lateinit var db: AppDatabase

    companion object {
        var REC_VIEW_TYPE: String = "EXPENSE"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_view)

        db = AppDatabase.getDatabase(this)

        recyTv = findViewById(R.id.recyTv)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadData()
        val btnBack = findViewById<TextView>(R.id.btnBack)
        btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            val transactions = db.transactionDao().getTransactionsByType(REC_VIEW_TYPE)
            
            recyTv.text = if (REC_VIEW_TYPE == "EXPENSE") "Expense List" else "Income List"
            
            val adapter = ExpenseAdapter(transactions)
            recyclerView.adapter = adapter
        }
    }

}