package com.example.network_base.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.network_base.R
import com.example.network_base.data.model.UserProfile
import com.example.network_base.data.model.UserRole
import com.example.network_base.data.repository.RoleManagementRepository
import com.example.network_base.databinding.FragmentRoleManagementBinding
import kotlinx.coroutines.launch

/**
 * Фрагмент для управления ролями пользователей
 */
class RoleManagementFragment : Fragment() {
    private var _binding: FragmentRoleManagementBinding? = null
    private val binding get() = _binding!!

    private val roleRepository = RoleManagementRepository()
    private lateinit var userAdapter: UserRoleAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoleManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настройка toolbar
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Настройка адаптера для списка пользователей
        userAdapter = UserRoleAdapter(
            onRoleChange = { userId, newRole ->
                changeUserRole(userId, newRole)
            }
        )
        binding.listUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.listUsers.adapter = userAdapter

        // Загрузка пользователей
        loadUsers()
    }

    private fun loadUsers() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val users = roleRepository.getAllUsers()
                userAdapter.submitList(users)
                binding.progressBar.visibility = View.GONE
                binding.listUsers.visibility = View.VISIBLE
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                // Показать ошибку
            }
        }
    }

    private fun changeUserRole(userId: String, newRole: UserRole) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val success = roleRepository.assignRole(userId, newRole)
                if (success) {
                    loadUsers() // Обновить список
                }
            } catch (e: Exception) {
                // Показать ошибку
            }
        }
    }
}
