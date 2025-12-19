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
import com.example.network_base.data.model.ModuleData
import com.example.network_base.data.repository.ModuleDataRepository
import com.example.network_base.databinding.FragmentModuleManagementBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ModuleManagementFragment : Fragment() {

    private var _binding: FragmentModuleManagementBinding? = null
    private val binding get() = _binding!!

    private val repository = ModuleDataRepository(FirebaseFirestore.getInstance())
    private lateinit var adapter: ModuleAdminAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentModuleManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настройка toolbar
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Настройка адаптера для списка модулей
        adapter = ModuleAdminAdapter(
            onClick = { /* Можно добавить переход к урокам модуля */ },
            onEdit = { showEditDialog(it) },
            onDelete = { confirmDelete(it) }
        )
        binding.listModules.layoutManager = LinearLayoutManager(requireContext())
        binding.listModules.adapter = adapter

        // Обработчик для кнопки добавления
        binding.fabAdd.setOnClickListener { showEditDialog(null) }

        // Загружаем модули
        loadModules()
    }

    private fun loadModules() {
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching { repository.loadModules() }
                .onSuccess { adapter.submitList(it) }
                .onFailure {
                    Toast.makeText(requireContext(), it.localizedMessage ?: "Ошибка загрузки", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun showEditDialog(module: ModuleData?) {
        val ctx = requireContext()
        val layout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 0)
        }
        val inputTitle = EditText(ctx).apply {
            hint = "Название модуля"
            setText(module?.title.orEmpty())
        }
        val inputDescription = EditText(ctx).apply {
            hint = "Описание модуля"
            setText(module?.description.orEmpty())
            minLines = 3
        }
        val inputOrder = EditText(ctx).apply {
            hint = "Порядок"
            setText(module?.order?.toString() ?: "0")
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        layout.addView(inputTitle)
        layout.addView(inputDescription)
        layout.addView(inputOrder)

        AlertDialog.Builder(ctx)
            .setTitle(if (module == null) "Новый модуль" else "Редактирование модуля")
            .setView(layout)
            .setPositiveButton("Сохранить") { _, _ ->
                val title = inputTitle.text.toString()
                val description = inputDescription.text.toString()
                val order = inputOrder.text.toString().toIntOrNull() ?: 0
                if (title.isBlank()) {
                    Toast.makeText(ctx, "Название не может быть пустым", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        if (module == null) {
                            repository.createModule(
                                title = title,
                                description = description,
                                order = order
                            )
                            Toast.makeText(ctx, "Модуль создан", Toast.LENGTH_SHORT).show()
                        } else {
                            repository.updateModule(
                                module.copy(
                                    title = title,
                                    description = description,
                                    order = order
                                )
                            )
                            Toast.makeText(ctx, "Модуль обновлен", Toast.LENGTH_SHORT).show()
                        }
                        loadModules()
                    } catch (e: Exception) {
                        Toast.makeText(ctx, "Ошибка: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun confirmDelete(module: ModuleData) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удаление модуля")
            .setMessage("Вы уверены, что хотите удалить модуль \"${module.title}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        repository.deleteModule(module.id)
                        Toast.makeText(requireContext(), "Модуль удален", Toast.LENGTH_SHORT).show()
                        loadModules()
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
