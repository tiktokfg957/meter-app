package com.example.meterreader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GoalAdapter(
    private val goals: List<Goal>,
    private val dbHelper: DatabaseHelper,
    private val onDataChanged: () -> Unit
) : RecyclerView.Adapter<GoalAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDescription: TextView = itemView.findViewById(R.id.tvGoalDescription)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressGoal)
        val tvProgress: TextView = itemView.findViewById(R.id.tvGoalProgress)
        val btnAddProgress: Button = itemView.findViewById(R.id.btnAddProgress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_goal, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val goal = goals[position]
        val meter = if (goal.meterId != 0L) dbHelper.getMeter(goal.meterId) else null
        val description = if (meter != null) "${meter.name}: цель %.2f руб".format(goal.targetAmount)
            else "Общая цель: %.2f руб".format(goal.targetAmount)
        holder.tvDescription.text = description
        holder.progressBar.max = goal.targetAmount.toInt()
        holder.progressBar.progress = goal.currentAmount.toInt()
        holder.tvProgress.text = "%.2f / %.2f руб".format(goal.currentAmount, goal.targetAmount)

        holder.btnAddProgress.setOnClickListener {
            // Здесь можно добавить диалог для добавления прогресса, но для простоты будем +10 руб
            goal.currentAmount += 10f
            if (goal.currentAmount >= goal.targetAmount) {
                goal.achieved = true
            }
            dbHelper.updateGoal(goal)
            onDataChanged()
        }
    }

    override fun getItemCount() = goals.size
}
