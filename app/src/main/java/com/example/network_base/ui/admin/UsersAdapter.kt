package com.example.network_base.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.network_base.R
import com.example.network_base.data.model.UserProfile
import com.example.network_base.data.model.UserRole
import com.example.network_base.databinding.ItemUserBinding

class UsersAdapter(
    private val onRoleChange: (UserProfile, String) -> Unit,
    private val onDelete: (UserProfile) -> Unit
) : ListAdapter<UserProfile, UsersAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding, onRoleChange, onDelete)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class UserViewHolder(
        private val binding: ItemUserBinding,
        private val onRoleChange: (UserProfile, String) -> Unit,
        private val onDelete: (UserProfile) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UserProfile) {
            binding.nameText.text = user.name
            binding.emailText.text = user.email ?: ""
            binding.roleChip.text = user.role.name
            binding.xpText.text = "${user.xp} XP"

            // Устанавливаем цвет роли
            val roleColor = when (user.role) {
                UserRole.ADMIN -> R.color.admin_role
                else -> R.color.user_role
            }
            binding.roleChip.setChipBackgroundColorResource(roleColor)

            // Обработчик меню
            binding.menuButton.setOnClickListener { view ->
                val popup = PopupMenu(view.context, view)
                popup.menuInflater.inflate(R.menu.menu_user_options, popup.menu)

                // Показываем опцию изменения роли
                popup.menu.findItem(R.id.action_change_role).isVisible = true

                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_change_role -> {
                            onRoleChange(user, user.role.name)
                            true
                        }
                        R.id.action_delete -> {
                            onDelete(user)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        }
    }
}

class UserDiffCallback : DiffUtil.ItemCallback<UserProfile>() {
    override fun areItemsTheSame(oldItem: UserProfile, newItem: UserProfile): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: UserProfile, newItem: UserProfile): Boolean {
        return oldItem == newItem
    }
}
