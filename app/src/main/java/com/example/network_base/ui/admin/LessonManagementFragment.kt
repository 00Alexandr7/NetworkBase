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
import com.example.network_base.data.model.Lesson
import com.example.network_base.data.repository.LessonRepository
import com.example.network_base.databinding.FragmentLessonManagementBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class LessonManagementFragment : Fragment() {

    private var _binding: FragmentLessonManagementBinding? = null
    private val binding get() = _binding!!

    private val repository = LessonRepository(FirebaseFirestore.getInstance())
    private lateinit var adapter: LessonAdminAdapter
    private lateinit var moduleId: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLessonManagementBinding.inflate(inflater, container, false)
        moduleId = arguments?.getString("moduleId") ?: ""
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настройка toolbar
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Настройка адаптера для списка уроков
        adapter = LessonAdminAdapter(
            onClick = { /* Можно добавить переход к содержимому урока */ },
            onEdit = { showEditDialog(it) },
            onDelete = { confirmDelete(it) }
        )
        binding.listLessons.layoutManager = LinearLayoutManager(requireContext())
        binding.listLessons.adapter = adapter

        // Обработчик для кнопки добавления
        binding.fabAdd.setOnClickListener { showEditDialog(null) }

        // Загружаем уроки
        loadLessons()
    }

    private fun loadLessons() {
        viewLifecycleOwner.lifecycleScope.launch {
            val loader = if (moduleId.isBlank()) {
                suspend { repository.loadLessons() }
            } else {
                suspend { repository.loadLessonsByModule(moduleId) }
            }

            runCatching { loader() }
                .onSuccess { adapter.submitList(it) }
                .onFailure {
                    Toast.makeText(requireContext(), it.localizedMessage ?: "Ошибка загрузки", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun showEditDialog(lesson: Lesson?) {
        val ctx = requireContext()
        val layout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 0)
        }
        val inputTitle = EditText(ctx).apply {
            hint = "Название урока"
            setText(lesson?.title.orEmpty())
        }
        val inputDescription = EditText(ctx).apply {
            hint = "Описание урока"
            setText(lesson?.description.orEmpty())
            minLines = 3
        }
        val inputType = EditText(ctx).apply {
            hint = "Тип урока"
            setText(lesson?.type.orEmpty())
        }
        val inputOrder = EditText(ctx).apply {
            hint = "Порядок"
            setText(lesson?.order?.toString() ?: "0")
            this.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        layout.addView(inputTitle)
        layout.addView(inputDescription)
        layout.addView(inputType)
        layout.addView(inputOrder)

        AlertDialog.Builder(ctx)
            .setTitle(if (lesson == null) "Новый урок" else "Редактирование урока")
            .setView(layout)
            .setPositiveButton("Сохранить") { _, _ ->
                val title = inputTitle.text.toString()
                val description = inputDescription.text.toString()
                val type = inputType.text.toString()
                val order = inputOrder.text.toString().toIntOrNull() ?: 0
                if (title.isBlank()) {
                    Toast.makeText(ctx, "Название не может быть пустым", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        if (lesson == null) {
                            repository.createLesson(
                                title = title,
                                description = description,
                                moduleId = moduleId,
                                content = "",
                                type = type,
                                order = order,
                                isPublished = false
                            )
                            Toast.makeText(ctx, "Урок создан", Toast.LENGTH_SHORT).show()
                        } else {
                            repository.updateLesson(
                                lesson.copy(
                                    title = title,
                                    description = description,
                                    type = type,
                                    order = order
                                )
                            )
                            Toast.makeText(ctx, "Урок обновлен", Toast.LENGTH_SHORT).show()
                        }
                        loadLessons()
                    } catch (e: Exception) {
                        Toast.makeText(ctx, "Ошибка: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun confirmDelete(lesson: Lesson) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удаление урока")
            .setMessage("Вы уверены, что хотите удалить урок \"${lesson.title}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        repository.deleteLesson(lesson.id)
                        Toast.makeText(requireContext(), "Урок удален", Toast.LENGTH_SHORT).show()
                        loadLessons()
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
