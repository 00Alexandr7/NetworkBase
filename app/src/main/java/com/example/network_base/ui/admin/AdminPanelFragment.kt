package com.example.network_base.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.network_base.R
import com.example.network_base.data.model.Task
import com.example.network_base.data.model.UserRole
import com.example.network_base.data.repository.ModuleDataRepository
import com.example.network_base.data.repository.LessonRepository
import com.example.network_base.data.repository.TaskRepository
import com.example.network_base.data.repository.TheoryDataRepository
import com.example.network_base.databinding.FragmentAdminPanelBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminPanelFragment : Fragment() {

    private var _binding: FragmentAdminPanelBinding? = null
    private val binding get() = _binding!!

    private val taskRepository = TaskRepository(FirebaseFirestore.getInstance())
    private val moduleRepository = ModuleDataRepository(FirebaseFirestore.getInstance())
    private lateinit var taskAdapter: TaskAdminAdapter

    // Проверка прав администратора
    private suspend fun checkAdminRights(): Boolean {
        return try {
            val auth = FirebaseAuth.getInstance()
            val uid = auth.currentUser?.uid
            if (uid != null) {
                val firestore = FirebaseFirestore.getInstance()
                // Получаем токен пользователя с правами
                val token = auth.currentUser?.getIdToken(false)?.await()?.token
                if (token != null) {
                    val snapshot = firestore.collection("users").document(uid).get().await()
                    UserRole.fromString(snapshot.getString("role")) == UserRole.ADMIN
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminPanelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Проверяем права администратора
        viewLifecycleOwner.lifecycleScope.launch {
            if (!checkAdminRights()) {
                Toast.makeText(requireContext(), "Доступ запрещен", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
                return@launch
            }
        }

        // Настройка toolbar
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Настройка адаптера для списка задач
        taskAdapter = TaskAdminAdapter(
            onClick = { showEditDialog(it) },
            onEdit = { showEditDialog(it) },
            onDelete = { confirmDelete(it) }
        )
        binding.listTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.listTasks.adapter = taskAdapter

        // Обработчики для кнопок
        binding.fabAdd.setOnClickListener { showEditDialog(null) }

        binding.btnManageUsers.setOnClickListener {
            findNavController().navigate(R.id.action_adminPanelFragment_to_usersManagementFragment)
        }

        binding.btnManageModules.setOnClickListener {
            findNavController().navigate(R.id.action_adminPanelFragment_to_modulesManagementFragment)
        }

        binding.btnManageLessons.setOnClickListener {
            findNavController().navigate(R.id.action_adminPanelFragment_to_lessonManagementFragment)
        }

        binding.btnManageTasks.setOnClickListener {
            findNavController().navigate(R.id.action_adminPanelFragment_to_taskManagementFragment)
        }

        binding.btnManageTheory.setOnClickListener {
            findNavController().navigate(R.id.action_adminPanelFragment_to_theoryManagementFragment)
        }

        // Загружаем задачи при первом запуске
        loadData()
    }

    private fun loadData() {
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching { taskRepository.loadTasks() }
                .onSuccess { taskAdapter.submitList(it) }
                .onFailure {
                    Toast.makeText(requireContext(), it.localizedMessage ?: "Ошибка загрузки", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun showEditDialog(task: Task?) {
        val ctx = requireContext()
        val layout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 0)
        }
        val inputTitle = EditText(ctx).apply {
            hint = getString(R.string.admin_task_title)
            setText(task?.title.orEmpty())
        }
        val inputDescription = EditText(ctx).apply {
            hint = getString(R.string.admin_task_description)
            setText(task?.title.orEmpty())
        }
        val inputModule = EditText(ctx).apply {
            hint = getString(R.string.admin_task_module)
            setText(task?.lessonId.orEmpty())
        }
        layout.addView(inputTitle)
        layout.addView(inputDescription)
        layout.addView(inputModule)

        AlertDialog.Builder(ctx)
            .setTitle(if (task == null) R.string.admin_add_task else R.string.admin_edit_task)
            .setView(layout)
            .setPositiveButton(R.string.save) { _, _ ->
                val title = inputTitle.text.toString()
                val desc = inputDescription.text.toString()
                val module = inputModule.text.toString()
                if (title.length < 2) {
                    Toast.makeText(ctx, R.string.validation_required, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                viewLifecycleOwner.lifecycleScope.launch {
                    if (task == null) {
                        taskRepository.createTask(title, "", "task")
                    } else {
                        taskRepository.updateTask(task.copy(title = title))
                    }
                    loadData()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun confirmDelete(task: Task) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.admin_delete_confirm_title)
            .setMessage(getString(R.string.admin_delete_confirm_body, task.title))
            .setPositiveButton(R.string.delete) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    taskRepository.deleteTask(task.id)
                    loadData()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
