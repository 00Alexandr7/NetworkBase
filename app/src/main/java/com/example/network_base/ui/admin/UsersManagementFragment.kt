package com.example.network_base.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.network_base.R
import com.example.network_base.data.model.UserProfile
import com.example.network_base.data.model.UserRole
import com.example.network_base.data.repository.UserManagementRepository
import com.example.network_base.databinding.FragmentUsersManagementBinding
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class UsersManagementFragment : Fragment() {

    private var _binding: FragmentUsersManagementBinding? = null
    private val binding get() = _binding!!

    private val repository = UserManagementRepository(
        FirebaseFirestore.getInstance(),
        FirebaseAuth.getInstance()
    )
    private lateinit var adapter: UsersAdapter

    private var allUsers: List<UserProfile> = emptyList()
    private var roleFilter: UserRole? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsersManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = UsersAdapter(
            onRoleChange = { user, newRole -> showRoleChangeDialog(user, newRole) },
            onDelete = { user -> confirmDelete(user) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        setupToolbar()

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        loadUsers()
    }

    private fun setupToolbar() {
        binding.toolbar.menu.clear()
        binding.toolbar.inflateMenu(R.menu.menu_users_management)
        binding.toolbar.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.action_filter_all -> {
                    roleFilter = null
                    applyFilter()
                    true
                }
                R.id.action_filter_admin -> {
                    roleFilter = UserRole.ADMIN
                    applyFilter()
                    true
                }
                R.id.action_filter_user -> {
                    roleFilter = UserRole.USER
                    applyFilter()
                    true
                }
                else -> false
            }
        }
    }

    private fun loadUsers() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                binding.textEmpty.visibility = View.GONE
                val users = repository.getAllUsers()
                allUsers = users
                applyFilter()
                binding.progressBar.visibility = View.GONE
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.textEmpty.visibility = View.VISIBLE
                val msg = when (e) {
                    is FirebaseFirestoreException -> {
                        if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                            "Нет доступа к списку пользователей (PERMISSION_DENIED). Проверь правила Firestore для коллекции users."
                        } else {
                            "Ошибка Firestore: ${e.code}"
                        }
                    }
                    else -> e.localizedMessage ?: "Ошибка загрузки пользователей"
                }
                binding.textEmpty.text = msg
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun applyFilter() {
        val filtered = when (val f = roleFilter) {
            null -> allUsers
            else -> allUsers.filter { it.role == f }
        }
        adapter.submitList(filtered)
        binding.textEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showRoleChangeDialog(user: UserProfile, newRole: String) {
        val roles = arrayOf("guest", "user", "admin")
        val currentRoleIndex = when (user.role) {
            UserRole.GUEST -> 0
            UserRole.USER -> 1
            UserRole.ADMIN -> 2
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Изменить роль пользователя: ${user.name}")
            .setSingleChoiceItems(roles, currentRoleIndex) { dialog, which ->
                dialog.dismiss()
                val selectedRole = roles[which]
                val selectedRoleEnum = when (selectedRole) {
                    "guest" -> UserRole.GUEST
                    "user" -> UserRole.USER
                    "admin" -> UserRole.ADMIN
                    else -> UserRole.GUEST
                }
                if (selectedRoleEnum != user.role) {
                    updateUserRole(user, selectedRole)
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun updateUserRole(user: UserProfile, newRole: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                val result = repository.updateUserRole(user.id, newRole)
                binding.progressBar.visibility = View.GONE

                result.onSuccess {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    loadUsers()
                }.onFailure { e ->
                    Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun confirmDelete(user: UserProfile) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удаление пользователя")
            .setMessage("Вы уверены, что хотите удалить пользователя ${user.name}?")
            .setPositiveButton("Удалить") { _, _ ->
                deleteUser(user)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun deleteUser(user: UserProfile) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                val result = repository.deleteUser(user.id)
                binding.progressBar.visibility = View.GONE

                result.onSuccess {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    loadUsers()
                }.onFailure { e ->
                    Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
