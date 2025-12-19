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
import com.example.network_base.data.repository.TaskRepository
import com.example.network_base.databinding.FragmentTaskManagementBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class TaskManagementFragment : Fragment() {

    private var _binding: FragmentTaskManagementBinding? = null
    private val binding get() = _binding!!

    private val repository = TaskRepository(FirebaseFirestore.getInstance())
    private lateinit var adapter: TaskAdminAdapter
    private lateinit var lessonId: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskManagementBinding.inflate(inflater, container, false)
        lessonId = arguments?.getString("lessonId") ?: ""
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настройка toolbar
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Настройка адаптера для списка задач
        adapter = TaskAdminAdapter(
            onClick = { /* Можно добавить переход к задаче */ },
            onEdit = { showEditDialog(it) },
            onDelete = { confirmDelete(it) }
        )
        binding.listTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.listTasks.adapter = adapter

        // Обработчик для кнопки добавления
        binding.fabAdd.setOnClickListener { showEditDialog(null) }

        // Загружаем задачи
        loadTasks()
    }

    private fun loadTasks() {
        viewLifecycleOwner.lifecycleScope.launch {
            val loader = if (lessonId.isBlank()) {
                suspend { repository.loadTasks() }
            } else {
                suspend { repository.loadTasksByLesson(lessonId) }
            }

            runCatching { loader() }
                .onSuccess { adapter.submitList(it) }
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
            hint = "Название задачи"
            setText(task?.title.orEmpty())
        }
        val inputLessonId = EditText(ctx).apply {
            hint = "ID урока"
            setText(if (lessonId.isNotBlank()) lessonId else task?.lessonId.orEmpty())
        }
        val inputType = EditText(ctx).apply {
            hint = "Тип задачи"
            setText(task?.type.orEmpty())
        }

        layout.addView(inputTitle)
        layout.addView(inputLessonId)
        layout.addView(inputType)

        AlertDialog.Builder(ctx)
            .setTitle(if (task == null) "Новая задача" else "Редактирование задачи")
            .setView(layout)
            .setPositiveButton("Сохранить") { _, _ ->
                val title = inputTitle.text.toString()
                val lessonIdValue = inputLessonId.text.toString().trim()
                val type = inputType.text.toString()
                if (title.isBlank()) {
                    Toast.makeText(ctx, "Название не может быть пустым", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (lessonIdValue.isBlank()) {
                    Toast.makeText(ctx, "ID урока не может быть пустым", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        if (task == null) {
                            repository.createTask(
                                title = title,
                                lessonId = lessonIdValue,
                                type = type,
                                isPublished = false
                            )
                            Toast.makeText(ctx, "Задача создана", Toast.LENGTH_SHORT).show()
                        } else {
                            repository.updateTask(
                                task.copy(
                                    title = title,
                                    lessonId = lessonIdValue,
                                    type = type
                                )
                            )
                            Toast.makeText(ctx, "Задача обновлена", Toast.LENGTH_SHORT).show()
                        }
                        loadTasks()
                    } catch (e: Exception) {
                        Toast.makeText(ctx, "Ошибка: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun confirmDelete(task: Task) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удаление задачи")
            .setMessage("Вы уверены, что хотите удалить задачу \"${task.title}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        repository.deleteTask(task.id)
                        Toast.makeText(requireContext(), "Задача удалена", Toast.LENGTH_SHORT).show()
                        loadTasks()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Ошибка: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
