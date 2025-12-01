package com.example.network_base.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.network_base.R
import com.example.network_base.databinding.ItemAchievementBinding

class AchievementAdapter : ListAdapter<AchievementWithStatus, AchievementAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAchievementBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ViewHolder(
        private val binding: ItemAchievementBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: AchievementWithStatus) {
            val achievement = item.achievement
            
            binding.textTitle.text = achievement.title
            binding.textDescription.text = achievement.description
            binding.textXp.text = "+${achievement.xpReward} XP"
            
            val iconColor = if (item.isUnlocked) {
                R.color.achievement_unlocked
            } else {
                R.color.achievement_locked
            }
            
            binding.iconAchievement.setColorFilter(
                ContextCompat.getColor(itemView.context, iconColor)
            )
            
            // Dim locked achievements
            binding.root.alpha = if (item.isUnlocked) 1f else 0.6f
            
            // Hide XP for unlocked achievements
            binding.textXp.visibility = if (item.isUnlocked) {
                android.view.View.GONE
            } else {
                android.view.View.VISIBLE
            }
        }
    }
    
    class DiffCallback : DiffUtil.ItemCallback<AchievementWithStatus>() {
        override fun areItemsTheSame(
            oldItem: AchievementWithStatus,
            newItem: AchievementWithStatus
        ): Boolean {
            return oldItem.achievement.id == newItem.achievement.id
        }
        
        override fun areContentsTheSame(
            oldItem: AchievementWithStatus,
            newItem: AchievementWithStatus
        ): Boolean {
            return oldItem == newItem
        }
    }
}

