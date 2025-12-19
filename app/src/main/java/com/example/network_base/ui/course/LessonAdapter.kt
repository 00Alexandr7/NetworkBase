package com.example.network_base.ui.course

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.network_base.R
import com.example.network_base.data.model.LessonWithContent
import com.example.network_base.databinding.ItemLessonBinding

class LessonAdapter(
    private val onLessonClick: (LessonWithContent) -> Unit
) : ListAdapter<LessonWithProgress, LessonAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLessonBinding.inflate(
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
        private val binding: ItemLessonBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: LessonWithProgress) {
            val lesson = item.lesson
            
            binding.textLessonTitle.text = lesson.title
            binding.textLessonDuration.text = "~${lesson.estimatedMinutes} мин"
            
            val iconRes = if (item.isCompleted) R.drawable.ic_check else R.drawable.ic_lesson
            val iconColor = if (item.isCompleted) R.color.success else R.color.text_hint
            
            binding.iconStatus.setImageResource(iconRes)
            binding.iconStatus.setColorFilter(
                ContextCompat.getColor(itemView.context, iconColor)
            )
            
            binding.root.setOnClickListener {
                onLessonClick(lesson)
            }
        }
    }
    
    class DiffCallback : DiffUtil.ItemCallback<LessonWithProgress>() {
        override fun areItemsTheSame(
            oldItem: LessonWithProgress,
            newItem: LessonWithProgress
        ): Boolean {
            return oldItem.lesson.id == newItem.lesson.id
        }
        
        override fun areContentsTheSame(
            oldItem: LessonWithProgress,
            newItem: LessonWithProgress
        ): Boolean {
            return oldItem == newItem
        }
    }
}

