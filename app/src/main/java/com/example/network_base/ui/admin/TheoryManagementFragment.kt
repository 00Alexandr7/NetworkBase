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
import androidx.recyclerview.widget.RecyclerView
import com.example.network_base.R
import com.example.network_base.data.model.ModuleData
import com.example.network_base.data.model.Theory
import com.example.network_base.data.repository.ModuleDataRepository
import com.example.network_base.data.repository.TheoryDataRepository
import com.example.network_base.databinding.FragmentTheoryManagementBinding
import com.example.network_base.ui.admin.TheoryAdapter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class TheoryManagementFragment : Fragment() {

    private var _binding: FragmentTheoryManagementBinding? = null
    private val binding get() = _binding!!

    private val repository = TheoryDataRepository(FirebaseFirestore.getInstance())
    private val moduleRepository = ModuleDataRepository(FirebaseFirestore.getInstance())
    private lateinit var adapter: TheoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTheoryManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настройка toolbar
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Настройка адаптера для списка теорий
        adapter = TheoryAdapter(
            onClick = { /* Можно добавить просмотр теории */ },
            onEdit = { showEditDialog(it) },
            onDelete = { confirmDelete(it) }
        )
        binding.listTheories.layoutManager = LinearLayoutManager(requireContext())
        binding.listTheories.adapter = adapter

        // Обработчик для кнопки добавления
        binding.fabAdd.setOnClickListener { showEditDialog(null) }

        // Загружаем теории
        loadTheories()
    }

    private fun loadTheories() {
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching { repository.loadTheories() }
                .onSuccess { adapter.submitList(it) }
                .onFailure {
                    Toast.makeText(requireContext(), it.localizedMessage ?: "Ошибка загрузки", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun showEditDialog(theory: Theory?) {
        viewLifecycleOwner.lifecycleScope.launch {
            val modules = runCatching { moduleRepository.loadModules() }
                .getOrElse { emptyList() }

            showEditDialogInternal(theory, modules)
        }
    }

    private fun showEditDialogInternal(theory: Theory?, modules: List<ModuleData>) {
        val ctx = requireContext()
        val layout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 0)
        }

        var selectedModule: ModuleData? = modules.find { it.id == theory?.moduleId }
            ?: modules.firstOrNull()

        val inputModule = EditText(ctx).apply {
            hint = "Модуль"
            isFocusable = false
            isClickable = true
            setText(selectedModule?.title ?: "Не выбран")

            setOnClickListener {
                if (modules.isEmpty()) {
                    Toast.makeText(ctx, "Сначала создайте модули", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val titles = modules.map { it.title }.toTypedArray()
                AlertDialog.Builder(ctx)
                    .setTitle("Выберите модуль")
                    .setItems(titles) { _, which ->
                        selectedModule = modules[which]
                        setText(selectedModule?.title ?: "Не выбран")
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
            }
        }
        val inputTitle = EditText(ctx).apply {
            hint = "Название теории"
            setText(theory?.title.orEmpty())
        }
        val inputContent = EditText(ctx).apply {
            hint = "Содержание теории"
            setText(theory?.content.orEmpty())
            minLines = 5
        }
        val inputOrder = EditText(ctx).apply {
            hint = "Порядок"
            setText(theory?.order?.toString() ?: "0")
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        layout.addView(inputModule)
        layout.addView(inputTitle)
        layout.addView(inputContent)
        layout.addView(inputOrder)

        AlertDialog.Builder(ctx)
            .setTitle(if (theory == null) "Новая теория" else "Редактирование теории")
            .setView(layout)
            .setPositiveButton("Сохранить") { _, _ ->
                val moduleId = selectedModule?.id ?: theory?.moduleId.orEmpty()
                val title = inputTitle.text.toString()
                val content = inputContent.text.toString()
                val order = inputOrder.text.toString().toIntOrNull() ?: 0
                if (title.isBlank()) {
                    Toast.makeText(ctx, "Название не может быть пустым", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (moduleId.isBlank()) {
                    Toast.makeText(ctx, "Выберите модуль", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        if (theory == null) {
                            repository.createTheory(
                                moduleId = moduleId,
                                title = title,
                                content = content,
                                order = order
                            )
                            Toast.makeText(ctx, "Теория создана", Toast.LENGTH_SHORT).show()
                        } else {
                            repository.updateTheory(theory.copy(moduleId = moduleId, title = title, content = content, order = order))
                            Toast.makeText(ctx, "Теория обновлена", Toast.LENGTH_SHORT).show()
                        }
                        loadTheories()
                    } catch (e: Exception) {
                        Toast.makeText(ctx, "Ошибка: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun confirmDelete(theory: Theory) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удаление теории")
            .setMessage("Вы уверены, что хотите удалить теорию \"${theory.title}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        repository.deleteTheory(theory.id)
                        Toast.makeText(requireContext(), "Теория удалена", Toast.LENGTH_SHORT).show()
                        loadTheories()
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
