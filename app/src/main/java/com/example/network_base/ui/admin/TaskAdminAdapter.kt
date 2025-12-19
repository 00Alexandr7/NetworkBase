package com.example.network_base.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.network_base.data.model.Task
import com.example.network_base.databinding.ItemTaskAdminBinding

class TaskAdminAdapter(
    private val onClick: (Task) -> Unit,
    private val onEdit: (Task) -> Unit,
    private val onDelete: (Task) -> Unit
) : ListAdapter<Task, TaskAdminAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskAdminBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding, onClick, onEdit, onDelete)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TaskViewHolder(
        private val binding: ItemTaskAdminBinding,
        private val onClick: (Task) -> Unit,
        private val onEdit: (Task) -> Unit,
        private val onDelete: (Task) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.title.text = task.title
            binding.lessonId.text = "Урок ID: ${task.lessonId}"
            binding.type.text = "Тип: ${task.type}"
            binding.publishedStatus.isChecked = task.isPublished

            binding.root.setOnClickListener {
                onClick(task)
            }

            binding.btnEdit.setOnClickListener {
                onEdit(task)
            }

            binding.btnDelete.setOnClickListener {
                onDelete(task)
            }

            binding.publishedStatus.setOnCheckedChangeListener { _, isChecked ->
                // Здесь можно добавить функционал для публикации/снятия с публикации задачи
            }
        }
    }

    private class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}
