package com.example.network_base.ui.course

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.network_base.R
import com.example.network_base.data.model.CourseModule
import com.example.network_base.databinding.ItemModuleBinding

class ModuleAdapter(
    private val onModuleClick: (CourseModule) -> Unit
) : ListAdapter<ModuleWithProgress, ModuleAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemModuleBinding.inflate(
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
        private val binding: ItemModuleBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: ModuleWithProgress) {
            val module = item.module
            val progress = item.progress
            
            binding.textModuleNumber.text = module.order.toString()
            binding.textModuleTitle.text = module.title
            binding.textModuleDescription.text = module.description
            
            val lessonCount = module.getLessonCount()
            binding.textLessonCount.text = "$lessonCount уроков"
            
            val taskStatus = if (progress?.taskCompleted == true) {
                "✓ Выполнено"
            } else {
                "1 задание"
            }
            binding.textTaskStatus.text = taskStatus
            
            // Update circle color based on completion
            val circleColor = if (progress?.taskCompleted == true) {
                ContextCompat.getColor(itemView.context, R.color.success)
            } else {
                ContextCompat.getColor(itemView.context, R.color.primary)
            }
            binding.circleBackground.background.setTint(circleColor)
            
            // Status icon
            val iconRes = when {
                progress?.taskCompleted == true -> R.drawable.ic_check
                progress != null && progress.lessonsCompleted.isNotEmpty() -> R.drawable.ic_chevron_right
                else -> R.drawable.ic_chevron_right
            }
            binding.iconStatus.setImageResource(iconRes)
            
            if (progress?.taskCompleted == true) {
                binding.iconStatus.setColorFilter(
                    ContextCompat.getColor(itemView.context, R.color.success)
                )
            } else {
                binding.iconStatus.setColorFilter(
                    ContextCompat.getColor(itemView.context, R.color.text_hint)
                )
            }
            
            binding.root.setOnClickListener {
                onModuleClick(module)
            }
        }
    }
    
    class DiffCallback : DiffUtil.ItemCallback<ModuleWithProgress>() {
        override fun areItemsTheSame(
            oldItem: ModuleWithProgress,
            newItem: ModuleWithProgress
        ): Boolean {
            return oldItem.module.id == newItem.module.id
        }
        
        override fun areContentsTheSame(
            oldItem: ModuleWithProgress,
            newItem: ModuleWithProgress
        ): Boolean {
            return oldItem == newItem
        }
    }
}

