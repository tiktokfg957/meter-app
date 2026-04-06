package com.example.meterreader.ui.achievements

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.meterreader.databinding.ItemAchievementBinding
import com.example.meterreader.utils.AchievementManager

class AchievementsAdapter(
    private val achievements: List<AchievementManager.Achievement>,
    private val onItemClick: (AchievementManager.Achievement) -> Unit
) : RecyclerView.Adapter<AchievementsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAchievementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(achievements[position])
    }

    override fun getItemCount() = achievements.size

    inner class ViewHolder(private val binding: ItemAchievementBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(achievement: AchievementManager.Achievement) {
            binding.tvTitle.text = achievement.name
            binding.tvDescription.text = achievement.description
            binding.tvReward.text = "Награда: ${achievement.rewardText}"
            binding.root.setOnClickListener { onItemClick(achievement) }
        }
    }
}
