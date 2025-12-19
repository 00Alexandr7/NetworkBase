package com.example.network_base.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.network_base.data.model.ModuleData
import com.example.network_base.databinding.ItemModuleAdminBinding

class ModuleAdminAdapter(
    private val onClick: (ModuleData) -> Unit,
    private val onEdit: (ModuleData) -> Unit,
    private val onDelete: (ModuleData) -> Unit
) : ListAdapter<ModuleData, ModuleAdminAdapter.ModuleViewHolder>(ModuleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val binding = ItemModuleAdminBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ModuleViewHolder(binding, onClick, onEdit, onDelete)
    }

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ModuleViewHolder(
        private val binding: ItemModuleAdminBinding,
        private val onClick: (ModuleData) -> Unit,
        private val onEdit: (ModuleData) -> Unit,
        private val onDelete: (ModuleData) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(module: ModuleData) {
            binding.title.text = module.title
            binding.description.text = module.description

            binding.root.setOnClickListener {
                onClick(module)
            }

            binding.btnEdit.setOnClickListener {
                onEdit(module)
            }

            binding.btnDelete.setOnClickListener {
                onDelete(module)
            }
        }
    }

    private class ModuleDiffCallback : DiffUtil.ItemCallback<ModuleData>() {
        override fun areItemsTheSame(oldItem: ModuleData, newItem: ModuleData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ModuleData, newItem: ModuleData): Boolean {
            return oldItem == newItem
        }
    }
}
