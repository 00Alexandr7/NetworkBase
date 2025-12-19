package com.example.network_base.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.network_base.data.model.Theory
import com.example.network_base.databinding.ItemTheoryAdminBinding

class TheoryAdapter(
    private val onClick: (Theory) -> Unit,
    private val onEdit: (Theory) -> Unit,
    private val onDelete: (Theory) -> Unit
) : ListAdapter<Theory, TheoryAdapter.TheoryViewHolder>(TheoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TheoryViewHolder {
        val binding = ItemTheoryAdminBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TheoryViewHolder(binding, onClick, onEdit, onDelete)
    }

    override fun onBindViewHolder(holder: TheoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TheoryViewHolder(
        private val binding: ItemTheoryAdminBinding,
        private val onClick: (Theory) -> Unit,
        private val onEdit: (Theory) -> Unit,
        private val onDelete: (Theory) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(theory: Theory) {
            binding.title.text = theory.title
            binding.description.text = if (theory.content.length > 100) {
                "${theory.content.take(100)}..."
            } else {
                theory.content
            }

            binding.root.setOnClickListener {
                onClick(theory)
            }

            binding.btnEdit.setOnClickListener {
                onEdit(theory)
            }

            binding.btnDelete.setOnClickListener {
                onDelete(theory)
            }
        }
    }

    private class TheoryDiffCallback : DiffUtil.ItemCallback<Theory>() {
        override fun areItemsTheSame(oldItem: Theory, newItem: Theory): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Theory, newItem: Theory): Boolean {
            return oldItem == newItem
        }
    }
}
