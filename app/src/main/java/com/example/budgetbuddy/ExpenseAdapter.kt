package com.example.budgetbuddy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExpenseAdapter(private val transactions: List<Transaction>) :
    RecyclerView.Adapter<ExpenseAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvReason: TextView = view.findViewById(R.id.tvReason)
        val tvBuy: TextView = view.findViewById(R.id.tvBuy)
        val tvType: TextView = view.findViewById(R.id.tvType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.data_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.tvReason.text = transaction.reason
        holder.tvType.text = transaction.type
        holder.tvBuy.text = "R %.2f".format(transaction.amount)
    }

    override fun getItemCount() = transactions.size
}