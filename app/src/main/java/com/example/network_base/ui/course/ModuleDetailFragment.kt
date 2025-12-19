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
import com.example.network_base.data.model.LessonWithContent
import com.example.network_base.data.model.ModuleProgress
import com.example.network_base.data.repository.CourseRepository
import com.example.network_base.data.repository.ProgressRepository
import com.example.network_base.databinding.FragmentModuleDetailBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ModuleDetailFragment : Fragment() {
    
    private var _binding: FragmentModuleDetailBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var courseRepository: CourseRepository
    private lateinit var progressRepository: ProgressRepository
    private lateinit var adapter: com.example.network_base.ui.course.LessonAdapter
    
    private var moduleId: String? = null
    private var module: CourseModule? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentModuleDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        moduleId = arguments?.getString("moduleId")
        
        val app = requireActivity().application as NetworkBaseApplication
        courseRepository = CourseRepository()
        progressRepository = ProgressRepository(app.database.progressDao())
        
        setupToolbar()
        setupRecyclerView()
        loadData()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = com.example.network_base.ui.course.LessonAdapter { lesson ->
            navigateToLesson(lesson)
        }
        
        binding.recyclerLessons.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerLessons.adapter = adapter
    }
    
    private fun loadData() {
        moduleId?.let { id ->
            module = courseRepository.getModuleById(id)
            module?.let { mod ->
                binding.toolbar.title = mod.title
                binding.textDescription.text = mod.description
                
                // Task card
                mod.task?.let { task ->
                    binding.textTaskTitle.text = task.title
                    binding.textTaskDescription.text = task.description
                    binding.textTaskXp.text = "+${task.xpReward} XP"
                    
                    binding.buttonStartTask.setOnClickListener {
                        navigateToTask(task.id)
                    }
                    
                    binding.cardTask.setOnClickListener {
                        navigateToTask(task.id)
                    }
                }
                
                // Observe progress
                viewLifecycleOwner.lifecycleScope.launch {
                    val initial = progressRepository.getOrCreateModuleProgress(id)
                    updateUI(mod, initial)

                    progressRepository.getModuleProgressFlow(id).collectLatest { progress ->
                        val p = progress ?: initial
                        updateUI(mod, p)
                    }
                }
            }
        }
    }
    
    private fun updateUI(module: CourseModule, progress: ModuleProgress) {
        // Lessons
        adapter.submitList(module.lessons.map { lesson ->
            LessonWithProgress(
                lesson = lesson,
                isCompleted = progress.lessonsCompleted.contains(lesson.id)
            )
        })
        
        // Progress
        val totalLessons = module.getLessonCount()
        val completedLessons = progress.lessonsCompleted.size
        val taskDone = if (progress.taskCompleted) 1 else 0
        val total = totalLessons + 1
        val completed = completedLessons + taskDone
        
        val percent = if (total > 0) (completed * 100 / total) else 0
        
        binding.progressCircular.progress = percent
        binding.textProgressTitle.text = "Прогресс: $percent%"
        binding.textProgressDetail.text = "$completedLessons из $totalLessons уроков, ${if (progress.taskCompleted) "задание выполнено" else "задание не выполнено"}"
        
        // Update task button
        binding.buttonStartTask.text = if (progress.taskCompleted) {
            "Пройдено (${progress.bestScore}%)"
        } else if (progress.taskAttempts > 0) {
            "Продолжить"
        } else {
            "Начать задание"
        }
    }
    
    private fun navigateToLesson(lesson: LessonWithContent) {
        val bundle = Bundle().apply {
            putString("lessonId", lesson.id)
        }
        findNavController().navigate(R.id.action_moduleDetail_to_lesson, bundle)
    }
    
    private fun navigateToTask(taskId: String) {
        val bundle = Bundle().apply {
            putString("taskId", taskId)
        }
        findNavController().navigate(R.id.action_moduleDetail_to_task, bundle)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class LessonWithProgress(
    val lesson: LessonWithContent,
    val isCompleted: Boolean
)

