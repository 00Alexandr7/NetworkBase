package com.example.network_base.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.network_base.data.model.UserRole
import com.example.network_base.databinding.ItemUserRoleBinding

/**
 * Адаптер для отображения пользователей и их ролей
 */
class UserRoleAdapter(
    private val onRoleChange: (String, UserRole) -> Unit
) : ListAdapter<Pair<com.example.network_base.data.model.UserProfile, UserRole>, UserRoleAdapter.UserRoleViewHolder>(UserRoleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserRoleViewHolder {
        val binding = ItemUserRoleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserRoleViewHolder(binding, onRoleChange)
    }

    override fun onBindViewHolder(holder: UserRoleViewHolder, position: Int) {
        val (user, role) = getItem(position)
        holder.bind(user, role)
    }

    class UserRoleViewHolder(
        private val binding: ItemUserRoleBinding,
        private val onRoleChange: (String, UserRole) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: com.example.network_base.data.model.UserProfile, role: UserRole) {
            binding.userName.text = user.name
            binding.userEmail.text = user.email ?: ""

            // Установка текущей роли
            binding.roleSpinner.setSelection(
                when (role) {
                    UserRole.GUEST -> 0
                    UserRole.USER -> 1
                    UserRole.ADMIN -> 2
                }
            )

            // Обработчик изменения роли
            binding.roleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val newRole = when (position) {
                        0 -> UserRole.GUEST
                        1 -> UserRole.USER
                        2 -> UserRole.ADMIN
                        else -> UserRole.GUEST
                    }

                    // Запрещаем назначать роль ADMIN
                    if (newRole == UserRole.ADMIN) {
                        // Возвращаем предыдущее значение в спиннер
                        binding.roleSpinner.setSelection(
                            when (role) {
                                UserRole.GUEST -> 0
                                UserRole.USER -> 1
                                UserRole.ADMIN -> 2
                            }
                        )
                        return
                    }

                    onRoleChange(user.id, newRole)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    class UserRoleDiffCallback : DiffUtil.ItemCallback<Pair<com.example.network_base.data.model.UserProfile, UserRole>>() {
        override fun areItemsTheSame(
            oldItem: Pair<com.example.network_base.data.model.UserProfile, UserRole>,
            newItem: Pair<com.example.network_base.data.model.UserProfile, UserRole>
        ): Boolean {
            return oldItem.first.id == newItem.first.id
        }

        override fun areContentsTheSame(
            oldItem: Pair<com.example.network_base.data.model.UserProfile, UserRole>,
            newItem: Pair<com.example.network_base.data.model.UserProfile, UserRole>
        ): Boolean {
            return oldItem == newItem
        }
    }
}
