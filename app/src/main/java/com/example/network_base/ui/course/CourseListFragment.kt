package com.example.network_base.ui.course

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.network_base.NetworkBaseApplication
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
        courseRepository = CourseRepository(requireContext())
        progressRepository = ProgressRepository(app.database.progressDao())
        
        setupRecyclerView()
        loadData()
    }
    
    private fun setupRecyclerView() {
        adapter = ModuleAdapter { module ->
            navigateToModule(module)
        }
        
        binding.recyclerModules.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerModules.adapter = adapter
    }
    
    private fun loadData() {
        val modules = courseRepository.getAllModules()
        
        viewLifecycleOwner.lifecycleScope.launch {
            progressRepository.getAllProgressFlow().collectLatest { progressList ->
                val progressMap = progressList.associateBy { it.moduleId }
                updateUI(modules, progressMap)
            }
        }
    }
    
    private fun updateUI(modules: List<CourseModule>, progressMap: Map<String, ModuleProgress>) {
        adapter.submitList(modules.map { module ->
            ModuleWithProgress(
                module = module,
                progress = progressMap[module.id]
            )
        })
        
        // Update overall progress
        val totalModules = modules.size
        val completedModules = modules.count { module ->
            progressMap[module.id]?.taskCompleted == true
        }
        
        val progressPercent = if (totalModules > 0) {
            (completedModules * 100 / totalModules)
        } else 0
        
        binding.textProgressPercent.text = "$progressPercent% завершено"
        binding.progressBar.progress = progressPercent
        
        // Find current module
        val currentModule = modules.firstOrNull { module ->
            progressMap[module.id]?.taskCompleted != true
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

