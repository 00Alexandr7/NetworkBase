package com.example.network_base.ui.course

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.network_base.NetworkBaseApplication
import com.example.network_base.MainActivity
import com.example.network_base.R
import com.example.network_base.data.model.CourseModule
import com.example.network_base.data.model.ModuleProgress
import com.example.network_base.data.repository.CourseRepository
import com.example.network_base.data.repository.ProgressRepository
import com.example.network_base.databinding.FragmentCourseListBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CourseListFragment : Fragment() {
    
    private var _binding: FragmentCourseListBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var courseRepository: CourseRepository
    private lateinit var progressRepository: ProgressRepository
    private lateinit var adapter: ModuleAdapter

    private var allModules: List<CourseModule> = emptyList()
    private var latestProgressMap: Map<String, ModuleProgress> = emptyMap()
    private var searchQuery: String = ""
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCourseListBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val app = requireActivity().application as NetworkBaseApplication
        courseRepository = CourseRepository()
        progressRepository = ProgressRepository(app.database.progressDao())
        
        setupToolbar()
        setupRecyclerView()
        loadData()
    }

    private fun setupToolbar() {
        binding.toolbar.menu.clear()
        binding.toolbar.inflateMenu(R.menu.menu_course_list)
        updateThemeIcon(binding.toolbar.menu.findItem(R.id.action_toggle_theme))

        val searchItem = binding.toolbar.menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as? SearchView
        searchView?.queryHint = "Поиск по модулям"
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchQuery = query.orEmpty()
                updateUI(allModules, latestProgressMap)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchQuery = newText.orEmpty()
                updateUI(allModules, latestProgressMap)
                return true
            }
        })

        binding.toolbar.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.action_toggle_theme -> {
                    (requireActivity() as? MainActivity)?.toggleTheme()
                    true
                }
                else -> false
            }
        }
    }

    private fun updateThemeIcon(item: MenuItem?) {
        val isNight = (requireActivity() as? MainActivity)?.isNightModeEnabled() == true
        item?.setIcon(if (isNight) R.drawable.ic_sun else R.drawable.ic_moon)
    }
    
    private fun setupRecyclerView() {
        adapter = ModuleAdapter { module ->
            navigateToModule(module)
        }
        
        binding.recyclerModules.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerModules.adapter = adapter
    }
    
    private fun loadData() {
        allModules = courseRepository.getAllModules()
        
        viewLifecycleOwner.lifecycleScope.launch {
            progressRepository.getAllProgressFlow().collectLatest { progressList ->
                latestProgressMap = progressList.associateBy { progress -> progress.moduleId }
                updateUI(allModules, latestProgressMap)
            }
        }
    }
    
    private fun updateUI(modules: List<CourseModule>, progressMap: Map<String, ModuleProgress>) {
        val q = searchQuery.trim().lowercase()
        val filteredModules = if (q.isBlank()) {
            modules
        } else {
            modules.filter { module ->
                module.title.lowercase().contains(q) ||
                    module.description.lowercase().contains(q)
            }
        }

        adapter.submitList(filteredModules.map { module ->
            ModuleWithProgress(
                module = module,
                progress = progressMap[module.id]
            )
        })
        
        // Update overall progress
        val totalModules = filteredModules.size
        val completedModules = filteredModules.count { module ->
            val p = progressMap[module.id]
            p != null && p.isCompleted(module.getLessonCount())
        }
        
        val progressPercent = if (totalModules > 0) {
            (completedModules * 100 / totalModules)
        } else 0
        
        binding.textProgressPercent.text = "$progressPercent% завершено"
        binding.progressBar.progress = progressPercent
        
        // Find current module
        val currentModule = filteredModules.firstOrNull { module ->
            val p = progressMap[module.id]
            p == null || !p.isCompleted(module.getLessonCount())
        }
        binding.textCurrentModule.text = currentModule?.let {
            "Текущий модуль: ${it.title}"
        } ?: "Все модули пройдены!"
    }
    
    private fun navigateToModule(module: CourseModule) {
        val bundle = Bundle().apply {
            putString("moduleId", module.id)
        }
        findNavController().navigate(R.id.action_courseList_to_moduleDetail, bundle)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class ModuleWithProgress(
    val module: CourseModule,
    val progress: ModuleProgress?
)

