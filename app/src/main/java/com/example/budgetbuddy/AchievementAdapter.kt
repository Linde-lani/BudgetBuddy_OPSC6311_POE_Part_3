package com.example.budgetbuddy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class AchievementAdapter(private val achievements: List<Achievement>) : RecyclerView.Adapter<AchievementAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.ivAchievementIcon)
        val title: TextView = view.findViewById(R.id.tvAchievementTitle)
        val description: TextView = view.findViewById(R.id.tvAchievementDescription)
        val status: ImageView = view.findViewById(R.id.ivLockedStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_achievement, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val achievement = achievements[position]
        holder.title.text = achievement.title
        holder.description.text = achievement.description

        if (achievement.isUnlocked) {
            holder.status.setImageResource(android.R.drawable.checkbox_on_background)
            holder.status.setColorFilter(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_dark))
            holder.icon.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.logo_yellow))
            holder.itemView.alpha = 1.0f
        } else {
            holder.status.setImageResource(android.R.drawable.ic_lock_lock)
            holder.status.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.text_secondary))
            holder.icon.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.text_secondary))
            holder.itemView.alpha = 0.5f
        }
    }

    override fun getItemCount() = achievements.size
}
