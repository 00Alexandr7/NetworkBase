package com.example.network_base.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.network_base.data.model.Lesson
import com.example.network_base.databinding.ItemLessonAdminBinding

class LessonAdminAdapter(
    private val onClick: (Lesson) -> Unit,
    private val onEdit: (Lesson) -> Unit,
    private val onDelete: (Lesson) -> Unit
) : ListAdapter<Lesson, LessonAdminAdapter.LessonViewHolder>(LessonDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val binding = ItemLessonAdminBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LessonViewHolder(binding, onClick, onEdit, onDelete)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class LessonViewHolder(
        private val binding: ItemLessonAdminBinding,
        private val onClick: (Lesson) -> Unit,
        private val onEdit: (Lesson) -> Unit,
        private val onDelete: (Lesson) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(lesson: Lesson) {
            binding.title.text = lesson.title
            binding.description.text = lesson.description

            binding.root.setOnClickListener {
                onClick(lesson)
            }

            binding.btnEdit.setOnClickListener {
                onEdit(lesson)
            }

            binding.btnDelete.setOnClickListener {
                onDelete(lesson)
            }
        }
    }

    private class LessonDiffCallback : DiffUtil.ItemCallback<Lesson>() {
        override fun areItemsTheSame(oldItem: Lesson, newItem: Lesson): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Lesson, newItem: Lesson): Boolean {
            return oldItem == newItem
        }
    }
}
